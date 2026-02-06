package com.findcomplain.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "business_ideas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BusinessIdea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(name = "problem_statement", columnDefinition = "TEXT")
    private String problemStatement;

    @Column(columnDefinition = "TEXT")
    private String solution;

    @Column(name = "target_market", columnDefinition = "TEXT")
    private String targetMarket;

    private String difficulty;

    @Column(name = "potential_score")
    private Integer potentialScore;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "source_complaints", columnDefinition = "jsonb")
    private List<Long> sourceComplaints;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
