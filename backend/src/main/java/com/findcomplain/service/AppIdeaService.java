package com.findcomplain.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.findcomplain.config.LlmConfig;
import com.findcomplain.domain.AppIdea;
import com.findcomplain.dto.AppIdeaAnalysisResult;
import com.findcomplain.dto.RedditPost;
import com.findcomplain.repository.AppIdeaRepository;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppIdeaService {

    private final LlmConfig llmConfig;
    private final AppIdeaRepository appIdeaRepository;
    private final RedditCrawlerService redditCrawler;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private ChatLanguageModel chatModel;

    @PostConstruct
    public void init() {
        if (!llmConfig.isMockMode() && llmConfig.getApiKey() != null && !llmConfig.getApiKey().isEmpty()) {
            chatModel = OpenAiChatModel.builder()
                    .apiKey(llmConfig.getApiKey())
                    .modelName(llmConfig.getModel())
                    .temperature(0.3)
                    .build();
            log.info("AppIdeaService: LLM initialized");
        }
    }

    public List<AppIdea> analyzeSubreddit(String subreddit, int limit) {
        log.info("Analyzing subreddit r/{} for app ideas", subreddit);

        List<RedditPost> posts = redditCrawler.fetchPosts(subreddit, null, limit);
        List<AppIdea> ideas = new ArrayList<>();

        for (RedditPost post : posts) {
            if (appIdeaRepository.existsByRedditPostId(post.getId())) {
                log.debug("Skipping already analyzed post: {}", post.getId());
                continue;
            }

            AppIdeaAnalysisResult result = analyzePost(post);

            if (result.isViable() && result.getViabilityScore() >= 5) {
                AppIdea idea = AppIdea.builder()
                        .redditPostId(post.getId())
                        .subreddit(subreddit)
                        .originalTitle(post.getTitle())
                        .originalContent(post.getSelftext())
                        .author(post.getAuthor())
                        .score(post.getScore())
                        .appName(result.getAppName())
                        .problemSummary(result.getProblemSummary())
                        .proposedSolution(result.getProposedSolution())
                        .targetUsers(result.getTargetUsers())
                        .keyFeatures(result.getKeyFeatures())
                        .techStack(result.getTechStack())
                        .difficulty(result.getDifficulty())
                        .viabilityScore(result.getViabilityScore())
                        .reasoning(result.getReasoning())
                        .redditCreatedAt(LocalDateTime.ofInstant(post.getCreatedUtc(), ZoneId.systemDefault()))
                        .build();

                ideas.add(appIdeaRepository.save(idea));
                log.info("Saved app idea: {} (score: {})", result.getAppName(), result.getViabilityScore());
            } else {
                log.debug("Not viable or low score: {}", post.getTitle());
            }
        }

        log.info("Found {} viable app ideas from r/{}", ideas.size(), subreddit);
        return ideas;
    }

    private AppIdeaAnalysisResult analyzePost(RedditPost post) {
        if (llmConfig.isMockMode() || chatModel == null) {
            return generateMockAnalysis(post);
        }

        return analyzePostWithLlm(post);
    }

    private AppIdeaAnalysisResult analyzePostWithLlm(RedditPost post) {
        String prompt = String.format("""
            You are an expert startup advisor and app developer. Analyze this Reddit post to see if it contains a viable app idea.

            Title: %s
            Content: %s
            Subreddit: r/%s

            Evaluate if this post describes:
            1. A real problem people face
            2. Something that could be solved with an app/software
            3. Has potential market demand

            Respond ONLY with valid JSON (no markdown):
            {
              "isViable": true,
              "appName": "Suggested App Name",
              "problemSummary": "Clear description of the problem in Korean",
              "proposedSolution": "How an app could solve this in Korean",
              "targetUsers": "Who would use this app in Korean",
              "keyFeatures": "3-5 key features separated by comma in Korean",
              "techStack": "Recommended tech stack (e.g., Flutter, React Native, etc.)",
              "difficulty": "easy/medium/hard",
              "viabilityScore": 7,
              "reasoning": "Why this is a good app idea in Korean"
            }

            If NOT viable, return:
            {"isViable":false,"appName":"","problemSummary":"","proposedSolution":"","targetUsers":"","keyFeatures":"","techStack":"","difficulty":"","viabilityScore":0,"reasoning":"Not viable because..."}

            Be strict: only mark as viable if it's a REAL app idea with clear problem and solution.
            viabilityScore should be 1-10 (10 being most viable)
            """,
            post.getTitle(),
            post.getSelftext().length() > 1000 ? post.getSelftext().substring(0, 1000) : post.getSelftext(),
            post.getSubreddit());

        try {
            String response = chatModel.generate(prompt);
            log.debug("LLM response for app idea: {}", response);
            String jsonStr = extractJson(response);
            return objectMapper.readValue(jsonStr, AppIdeaAnalysisResult.class);
        } catch (Exception e) {
            log.error("Failed to parse LLM response: {}", e.getMessage());
            return generateMockAnalysis(post);
        }
    }

    private String extractJson(String response) {
        response = response.replace("```json", "").replace("```", "").trim();
        int start = response.indexOf("{");
        int end = response.lastIndexOf("}") + 1;
        if (start >= 0 && end > start) {
            return response.substring(start, end);
        }
        return response;
    }

    private AppIdeaAnalysisResult generateMockAnalysis(RedditPost post) {
        AppIdeaAnalysisResult result = new AppIdeaAnalysisResult();

        String content = (post.getTitle() + " " + post.getSelftext()).toLowerCase();
        boolean hasAppPotential = content.contains("app") ||
                content.contains("wish") ||
                content.contains("need") ||
                content.contains("want") ||
                content.contains("idea") ||
                content.contains("someone make") ||
                content.contains("would pay");

        result.setViable(hasAppPotential);

        if (hasAppPotential) {
            result.setAppName("Mock App for: " + post.getTitle().substring(0, Math.min(30, post.getTitle().length())));
            result.setProblemSummary("사용자가 겪는 문제에 대한 요약");
            result.setProposedSolution("앱으로 해결할 수 있는 방안");
            result.setTargetUsers("일반 사용자");
            result.setKeyFeatures("기능1, 기능2, 기능3");
            result.setTechStack("Flutter, Firebase");
            result.setDifficulty("medium");
            result.setViabilityScore(6);
            result.setReasoning("Mock analysis - LLM not available");
        }

        return result;
    }

    public List<AppIdea> getAllIdeas() {
        return appIdeaRepository.findRecentIdeas();
    }

    public List<AppIdea> getTopIdeas() {
        return appIdeaRepository.findTopByViability();
    }

    public List<AppIdea> getBookmarkedIdeas() {
        return appIdeaRepository.findByBookmarkedTrue();
    }

    public AppIdea toggleBookmark(Long id) {
        AppIdea idea = appIdeaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Idea not found"));
        idea.setBookmarked(!idea.getBookmarked());
        return appIdeaRepository.save(idea);
    }
}
