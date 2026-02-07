package com.findcomplain.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "app_ideas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppIdea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String redditPostId;

    private String subreddit;

    @Column(columnDefinition = "TEXT")
    private String originalTitle;

    @Column(columnDefinition = "TEXT")
    private String originalContent;

    private String author;
    private Integer score;

    // LLM 분석 결과
    @Column(columnDefinition = "TEXT")
    private String appName;

    @Column(columnDefinition = "TEXT")
    private String problemSummary;

    @Column(columnDefinition = "TEXT")
    private String proposedSolution;

    @Column(columnDefinition = "TEXT")
    private String targetUsers;

    @Column(columnDefinition = "TEXT")
    private String keyFeatures;

    @Column(columnDefinition = "TEXT")
    private String techStack;

    private String difficulty; // easy, medium, hard

    private Integer viabilityScore; // 1-10

    @Column(columnDefinition = "TEXT")
    private String reasoning;

    private Boolean bookmarked;

    private LocalDateTime redditCreatedAt;
    private LocalDateTime analyzedAt;

    @PrePersist
    protected void onCreate() {
        analyzedAt = LocalDateTime.now();
        bookmarked = false;
    }
}
