package com.findcomplain.service;

import com.findcomplain.domain.AppIdea;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class IdeaDiscoveryScheduler {

    private final AppIdeaService appIdeaService;

    // 아이디어 발굴 대상 서브레딧
    private static final List<String> TARGET_SUBREDDITS = Arrays.asList(
            "SomebodyMakeThis",
            "AppIdeas",
            "mildlyinfuriating",
            "Lightbulb",
            "ProductivityApps",
            "startups",
            "SideProject",
            "indiehackers"
    );

    // 매일 오전 9시에 실행 (cron = "초 분 시 일 월 요일")
    @Scheduled(cron = "0 0 9 * * *")
    public void dailyIdeaDiscovery() {
        log.info("Starting daily idea discovery at {}", LocalDateTime.now());
        runDiscovery(25);
    }

    // 수동 실행용
    public int runDiscoveryNow() {
        return runDiscovery(20);
    }

    private int runDiscovery(int postsPerSubreddit) {
        int totalIdeas = 0;

        for (String subreddit : TARGET_SUBREDDITS) {
            try {
                log.info("Scanning r/{} for app ideas...", subreddit);
                List<AppIdea> ideas = appIdeaService.analyzeSubreddit(subreddit, postsPerSubreddit);
                totalIdeas += ideas.size();
                log.info("Found {} ideas from r/{}", ideas.size(), subreddit);

                // Rate limiting - Reddit API 제한 방지
                Thread.sleep(2000);
            } catch (Exception e) {
                log.error("Failed to analyze r/{}: {}", subreddit, e.getMessage());
            }
        }

        log.info("Daily discovery completed. Total new ideas: {}", totalIdeas);
        return totalIdeas;
    }

    public List<String> getTargetSubreddits() {
        return TARGET_SUBREDDITS;
    }
}
