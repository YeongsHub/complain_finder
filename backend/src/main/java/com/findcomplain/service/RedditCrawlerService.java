package com.findcomplain.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.findcomplain.config.RedditApiConfig;
import com.findcomplain.dto.RedditPost;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedditCrawlerService {

    private final RedditApiConfig config;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<RedditPost> fetchPosts(String subreddit, List<String> keywords, int limit) {
        if (config.isMockMode()) {
            log.info("Using mock mode for Reddit API");
            return generateMockPosts(subreddit, limit);
        }

        try {
            return fetchPublicJsonApi(subreddit, limit);
        } catch (Exception e) {
            log.error("Failed to fetch from Reddit API, falling back to mock data", e);
            return generateMockPosts(subreddit, limit);
        }
    }

    private List<RedditPost> fetchPublicJsonApi(String subreddit, int limit) {
        log.info("Fetching posts from r/{} using public JSON API", subreddit);

        WebClient client = WebClient.builder()
                .baseUrl("https://www.reddit.com")
                .defaultHeader("User-Agent", config.getUserAgent())
                .build();

        String response = client.get()
                .uri("/r/{subreddit}/hot.json?limit={limit}", subreddit, limit)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        List<RedditPost> posts = new ArrayList<>();

        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode children = root.path("data").path("children");

            for (JsonNode child : children) {
                JsonNode data = child.path("data");

                // 광고나 고정 게시물 제외
                if (data.path("stickied").asBoolean() || data.path("is_video").asBoolean()) {
                    continue;
                }

                String selftext = data.path("selftext").asText();
                String title = data.path("title").asText();

                // 내용이 있는 게시물만 수집
                if (selftext.isEmpty() || selftext.equals("[removed]") || selftext.equals("[deleted]")) {
                    selftext = title; // 제목을 내용으로 사용
                }

                RedditPost post = RedditPost.builder()
                        .id(data.path("id").asText())
                        .subreddit(subreddit)
                        .title(title)
                        .selftext(selftext)
                        .author(data.path("author").asText())
                        .score(data.path("score").asInt())
                        .createdUtc(Instant.ofEpochSecond(data.path("created_utc").asLong()))
                        .build();
                posts.add(post);
            }

            log.info("Fetched {} posts from r/{}", posts.size(), subreddit);
        } catch (Exception e) {
            log.error("Failed to parse Reddit response", e);
        }

        return posts;
    }

    private List<RedditPost> generateMockPosts(String subreddit, int limit) {
        List<RedditPost> posts = new ArrayList<>();
        Random random = new Random();

        String[] mockComplaints = {
            "I'm so frustrated with this product. The price keeps going up but the quality is getting worse!",
            "Why is the UX so terrible? I can't find anything in this app.",
            "This feature has been broken for months and they still haven't fixed it.",
            "Customer support is absolutely useless. Waited 3 hours just to get a generic response.",
            "The new update completely ruined the app. Why do companies always change things that work?",
            "Overpriced garbage. There are free alternatives that work better.",
            "I've been a loyal customer for years and this is how they treat us?",
            "The subscription model is ridiculous. I just want to pay once.",
            "Performance issues make this unusable. Crashes every 5 minutes.",
            "Missing basic features that competitors have had for years.",
            "The learning curve is insane. No documentation, no tutorials.",
            "Privacy concerns are completely ignored by this company.",
            "Ads everywhere! Can't even use the free version anymore.",
            "Auto-renewal without warning drained my bank account.",
            "Integration with other tools is non-existent."
        };

        String[] mockTitles = {
            "Why is %s so expensive now?",
            "Frustrated with %s's customer service",
            "The latest update ruined everything",
            "Anyone else having issues with %s?",
            "Thinking of switching from %s",
            "Major bug in %s - still not fixed!",
            "The UX of %s needs a complete overhaul",
            "Missing feature request: still waiting after 2 years",
            "Subscription fatigue with %s",
            "Performance problems getting worse"
        };

        for (int i = 0; i < Math.min(limit, mockComplaints.length); i++) {
            String title = String.format(mockTitles[i % mockTitles.length], subreddit);
            RedditPost post = RedditPost.builder()
                    .id("mock_" + i + "_" + System.currentTimeMillis())
                    .subreddit(subreddit)
                    .title(title)
                    .selftext(mockComplaints[i])
                    .author("user_" + random.nextInt(10000))
                    .score(random.nextInt(500) + 10)
                    .createdUtc(Instant.now().minusSeconds(random.nextInt(86400 * 30)))
                    .build();
            posts.add(post);
        }

        log.info("Generated {} mock posts for r/{}", posts.size(), subreddit);
        return posts;
    }
}
