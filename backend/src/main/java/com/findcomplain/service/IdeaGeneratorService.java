package com.findcomplain.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.findcomplain.config.LlmConfig;
import com.findcomplain.domain.BusinessIdea;
import com.findcomplain.domain.Complaint;
import com.findcomplain.dto.IdeaGenerationResult;
import com.findcomplain.repository.BusinessIdeaRepository;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class IdeaGeneratorService {

    private final LlmConfig llmConfig;
    private final BusinessIdeaRepository ideaRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private ChatLanguageModel chatModel;

    @PostConstruct
    public void init() {
        if (!llmConfig.isMockMode() && llmConfig.getApiKey() != null && !llmConfig.getApiKey().isEmpty()) {
            chatModel = OpenAiChatModel.builder()
                    .apiKey(llmConfig.getApiKey())
                    .modelName(llmConfig.getModel())
                    .temperature(0.7)
                    .build();
        }
    }

    public List<BusinessIdea> generateIdeas(List<Complaint> complaints, String subreddit) {
        if (complaints.isEmpty()) {
            log.info("No complaints to generate ideas from");
            return Collections.emptyList();
        }

        Map<String, List<Complaint>> clusteredComplaints = clusterByCategory(complaints);
        List<BusinessIdea> ideas = new ArrayList<>();

        for (Map.Entry<String, List<Complaint>> entry : clusteredComplaints.entrySet()) {
            if (entry.getValue().size() >= 2) {
                IdeaGenerationResult result = generateIdea(entry.getValue(), subreddit, entry.getKey());
                if (result != null) {
                    BusinessIdea idea = BusinessIdea.builder()
                            .title(result.getTitle())
                            .problemStatement(result.getProblem())
                            .solution(result.getSolution())
                            .targetMarket(result.getTargetMarket())
                            .difficulty(result.getDifficulty())
                            .potentialScore(result.getPotential())
                            .sourceComplaints(entry.getValue().stream()
                                    .map(Complaint::getId)
                                    .collect(Collectors.toList()))
                            .createdAt(LocalDateTime.now())
                            .build();

                    ideas.add(ideaRepository.save(idea));
                    log.info("Generated idea: {}", result.getTitle());
                }
            }
        }

        log.info("Generated {} ideas from {} complaints", ideas.size(), complaints.size());
        return ideas;
    }

    private Map<String, List<Complaint>> clusterByCategory(List<Complaint> complaints) {
        return complaints.stream()
                .filter(c -> c.getCategory() != null)
                .collect(Collectors.groupingBy(Complaint::getCategory));
    }

    private IdeaGenerationResult generateIdea(List<Complaint> complaints, String subreddit, String category) {
        if (llmConfig.isMockMode() || chatModel == null) {
            return generateMockIdea(complaints, subreddit, category);
        }

        return generateIdeaWithLlm(complaints, subreddit, category);
    }

    private IdeaGenerationResult generateIdeaWithLlm(List<Complaint> complaints, String subreddit, String category) {
        String complaintsSummary = complaints.stream()
                .map(c -> "- " + c.getExtractedProblem())
                .collect(Collectors.joining("\n"));

        String prompt = String.format("""
            다음은 r/%s 서브레딧에서 발견된 '%s' 카테고리의 주요 불평들입니다:

            %s

            이 불평들을 해결할 수 있는 비즈니스 아이디어를 제안하세요.

            반드시 아래 JSON 형식으로만 응답하세요 (다른 텍스트 없이):
            {
              "title": "아이디어 제목",
              "problem": "해결하려는 문제",
              "solution": "제안하는 솔루션",
              "targetMarket": "타겟 고객층",
              "difficulty": "easy" 또는 "medium" 또는 "hard",
              "potential": 1에서 10 사이의 숫자,
              "reasoning": "왜 이 아이디어가 가능성 있는지"
            }
            """, subreddit, category, complaintsSummary);

        try {
            String response = chatModel.generate(prompt);
            String jsonStr = extractJson(response);
            return objectMapper.readValue(jsonStr, IdeaGenerationResult.class);
        } catch (Exception e) {
            log.error("Failed to parse LLM response for idea generation", e);
            return generateMockIdea(complaints, subreddit, category);
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

    private IdeaGenerationResult generateMockIdea(List<Complaint> complaints, String subreddit, String category) {
        Map<String, IdeaTemplate> templates = new HashMap<>();
        templates.put("가격", new IdeaTemplate(
                "저렴한 %s 대안 서비스",
                "사용자들이 현재 서비스의 높은 가격에 불만을 느끼고 있습니다.",
                "무료 티어와 합리적인 가격의 프리미엄 플랜을 제공하는 대안 서비스 개발",
                "가격에 민감한 개인 사용자 및 소규모 팀",
                "medium", 7
        ));
        templates.put("UX", new IdeaTemplate(
                "직관적인 %s 인터페이스 도구",
                "복잡한 UX로 인해 사용자들이 어려움을 겪고 있습니다.",
                "AI 기반 온보딩과 간소화된 인터페이스를 제공하는 래퍼/플러그인 개발",
                "비기술 사용자 및 신규 사용자",
                "hard", 6
        ));
        templates.put("기능부족", new IdeaTemplate(
                "%s 기능 확장 플러그인",
                "사용자들이 필요로 하는 핵심 기능이 없습니다.",
                "가장 요청이 많은 기능들을 제공하는 서드파티 확장/플러그인 개발",
                "파워 유저 및 전문가 그룹",
                "medium", 8
        ));
        templates.put("버그", new IdeaTemplate(
                "안정적인 %s 대안",
                "기존 솔루션의 불안정성으로 인해 작업 손실이 발생합니다.",
                "안정성과 데이터 백업을 최우선으로 하는 경쟁 제품 개발",
                "데이터 손실에 민감한 전문가 그룹",
                "hard", 7
        ));
        templates.put("서비스", new IdeaTemplate(
                "%s 커뮤니티 지원 플랫폼",
                "고객 지원 품질이 낮아 사용자들이 좌절감을 느낍니다.",
                "커뮤니티 기반 지원 및 AI 챗봇을 활용한 즉각적인 도움 제공",
                "빠른 지원이 필요한 모든 사용자",
                "easy", 6
        ));
        templates.put("기타", new IdeaTemplate(
                "%s 개선 솔루션",
                "다양한 불만 사항이 발견되었습니다.",
                "주요 페인 포인트를 해결하는 올인원 솔루션 제공",
                "일반 사용자",
                "medium", 5
        ));

        IdeaTemplate template = templates.getOrDefault(category, templates.get("기타"));

        IdeaGenerationResult result = new IdeaGenerationResult();
        result.setTitle(String.format(template.title, subreddit));
        result.setProblem(template.problem);
        result.setSolution(template.solution);
        result.setTargetMarket(template.targetMarket);
        result.setDifficulty(template.difficulty);
        result.setPotential(template.potential);
        result.setReasoning(String.format("r/%s에서 %d건의 '%s' 관련 불평이 발견되어 시장 수요가 확인됨",
                subreddit, complaints.size(), category));

        return result;
    }

    private record IdeaTemplate(String title, String problem, String solution,
                                String targetMarket, String difficulty, int potential) {}
}
