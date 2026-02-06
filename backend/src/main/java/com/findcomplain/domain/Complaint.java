package com.findcomplain.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "complaints")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Complaint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reddit_post_id")
    private String redditPostId;

    private String subreddit;

    @Column(columnDefinition = "TEXT")
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    private String author;

    private Integer score;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // LLM 분석 결과
    private String category;

    @Column(name = "pain_level")
    private Integer painLevel;

    @Column(name = "extracted_problem", columnDefinition = "TEXT")
    private String extractedProblem;

    @Column(name = "analyzed_at")
    private LocalDateTime analyzedAt;
}
