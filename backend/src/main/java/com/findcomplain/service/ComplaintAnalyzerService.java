package com.findcomplain.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.findcomplain.config.LlmConfig;
import com.findcomplain.domain.Complaint;
import com.findcomplain.dto.ComplaintAnalysisResult;
import com.findcomplain.dto.RedditPost;
import com.findcomplain.repository.ComplaintRepository;
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
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class ComplaintAnalyzerService {

    private final LlmConfig llmConfig;
    private final ComplaintRepository complaintRepository;
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
            log.info("LLM initialized with model: {}", llmConfig.getModel());
        } else {
            log.info("LLM running in mock mode");
        }
    }

    public List<Complaint> analyzeAndSavePosts(List<RedditPost> posts) {
        List<Complaint> complaints = new ArrayList<>();

        for (RedditPost post : posts) {
            if (complaintRepository.existsByRedditPostId(post.getId())) {
                log.debug("Skipping already analyzed post: {}", post.getId());
                continue;
            }

            ComplaintAnalysisResult result = analyzePost(post);

            if (result.isComplaint()) {
                Complaint complaint = Complaint.builder()
                        .redditPostId(post.getId())
                        .subreddit(post.getSubreddit())
                        .title(post.getTitle())
                        .content(post.getSelftext())
                        .author(post.getAuthor())
                        .score(post.getScore())
                        .createdAt(LocalDateTime.ofInstant(post.getCreatedUtc(), ZoneId.systemDefault()))
                        .category(result.getCategory())
                        .painLevel(result.getPainLevel())
                        .extractedProblem(result.getCoreProblem())
                        .analyzedAt(LocalDateTime.now())
                        .build();

                complaints.add(complaintRepository.save(complaint));
                log.debug("Saved complaint: {} - Category: {}", post.getTitle(), result.getCategory());
            }
        }

        log.info("Analyzed {} posts, found {} complaints", posts.size(), complaints.size());
        return complaints;
    }

    private ComplaintAnalysisResult analyzePost(RedditPost post) {
        if (llmConfig.isMockMode() || chatModel == null) {
            return generateMockAnalysis(post);
        }

        return analyzePostWithLlm(post);
    }

    private ComplaintAnalysisResult analyzePostWithLlm(RedditPost post) {
        String prompt = String.format("""
            다음 Reddit 게시글에서 사용자의 불평/불만을 분석하세요.

            제목: %s
            내용: %s

            반드시 아래 JSON 형식으로만 응답하세요 (다른 텍스트 없이):
            {
              "isComplaint": true 또는 false,
              "category": "가격" 또는 "UX" 또는 "기능부족" 또는 "버그" 또는 "서비스" 또는 "기타",
              "painLevel": 1에서 5 사이의 숫자,
              "coreProblem": "핵심 문제 한 문장 요약",
              "keywords": ["관련", "키워드"]
            }
            """, post.getTitle(), post.getSelftext());

        try {
            String response = chatModel.generate(prompt);
            String jsonStr = extractJson(response);
            return objectMapper.readValue(jsonStr, ComplaintAnalysisResult.class);
        } catch (Exception e) {
            log.error("Failed to parse LLM response, using mock analysis", e);
            return generateMockAnalysis(post);
        }
    }

    private String extractJson(String response) {
        int start = response.indexOf("{");
        int end = response.lastIndexOf("}") + 1;
        if (start >= 0 && end > start) {
            return response.substring(start, end);
        }
        return response;
    }

    private ComplaintAnalysisResult generateMockAnalysis(RedditPost post) {
        Random random = new Random(post.getId().hashCode());
        String[] categories = {"가격", "UX", "기능부족", "버그", "서비스", "기타"};

        String content = (post.getTitle() + " " + post.getSelftext()).toLowerCase();
        boolean isComplaint = content.contains("frustrat") ||
                content.contains("hate") ||
                content.contains("terrible") ||
                content.contains("worst") ||
                content.contains("broken") ||
                content.contains("expensive") ||
                content.contains("useless") ||
                content.contains("bug") ||
                random.nextDouble() > 0.3;

        ComplaintAnalysisResult result = new ComplaintAnalysisResult();
        result.setComplaint(isComplaint);

        if (isComplaint) {
            String category;
            if (content.contains("price") || content.contains("expensive") || content.contains("cost")) {
                category = "가격";
            } else if (content.contains("ux") || content.contains("ui") || content.contains("design") || content.contains("find")) {
                category = "UX";
            } else if (content.contains("feature") || content.contains("missing")) {
                category = "기능부족";
            } else if (content.contains("bug") || content.contains("crash") || content.contains("broken")) {
                category = "버그";
            } else if (content.contains("support") || content.contains("customer")) {
                category = "서비스";
            } else {
                category = categories[random.nextInt(categories.length)];
            }

            result.setCategory(category);
            result.setPainLevel(random.nextInt(3) + 3);
            result.setCoreProblem(post.getTitle().length() > 100 ?
                    post.getTitle().substring(0, 100) : post.getTitle());
            result.setKeywords(List.of(post.getSubreddit(), category));
        }

        return result;
    }
}
