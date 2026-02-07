package com.findcomplain.service;

import com.findcomplain.domain.AppIdea;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
@RequiredArgsConstructor
@Slf4j
public class IdeaDiscoveryScheduler {

    private final AppIdeaService appIdeaService;

    // 디폴트 서브레딧 (삭제 불가)
    private static final List<String> DEFAULT_SUBREDDITS = List.of(
            "SomebodyMakeThis",
            "AppIdeas",
            "mildlyinfuriating",
            "Lightbulb",
            "ProductivityApps",
            "startups",
            "SideProject",
            "indiehackers"
    );

    // 사용자가 추가한 커스텀 서브레딧
    private final List<String> customSubreddits = new CopyOnWriteArrayList<>();

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

        for (String subreddit : getTargetSubreddits()) {
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
        List<String> all = new ArrayList<>(DEFAULT_SUBREDDITS);
        all.addAll(customSubreddits);
        return all;
    }

    public List<String> getDefaultSubreddits() {
        return DEFAULT_SUBREDDITS;
    }

    public List<String> getCustomSubreddits() {
        return List.copyOf(customSubreddits);
    }

    public String addCustomSubreddit(String subreddit) {
        String name = subreddit.trim();
        if (name.isEmpty()) {
            throw new IllegalArgumentException("Subreddit name cannot be empty");
        }
        boolean exists = getTargetSubreddits().stream()
                .anyMatch(s -> s.equalsIgnoreCase(name));
        if (exists) {
            throw new IllegalArgumentException("Subreddit '" + name + "' already exists");
        }
        customSubreddits.add(name);
        log.info("Added custom subreddit: r/{}", name);
        return name;
    }

    public void removeCustomSubreddit(String subreddit) {
        String name = subreddit.trim();
        boolean removed = customSubreddits.removeIf(s -> s.equalsIgnoreCase(name));
        if (!removed) {
            throw new IllegalArgumentException("Subreddit '" + name + "' is not a custom subreddit");
        }
        log.info("Removed custom subreddit: r/{}", name);
    }
}
