package com.findcomplain.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@Builder
public class RedditPost {
    private String id;
    private String subreddit;
    private String title;
    private String selftext;
    private String author;
    private int score;
    private Instant createdUtc;
    private List<RedditComment> comments;

    @Data
    @Builder
    public static class RedditComment {
        private String id;
        private String body;
        private String author;
        private int score;
    }
}
