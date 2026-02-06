package com.findcomplain.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "analysis_sessions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalysisSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String subreddit;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(columnDefinition = "text[]")
    private List<String> keywords;

    @Column(name = "total_posts")
    private Integer totalPosts;

    @Column(name = "total_complaints")
    private Integer totalComplaints;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Enumerated(EnumType.STRING)
    private AnalysisStatus status;

    @PrePersist
    protected void onCreate() {
        startedAt = LocalDateTime.now();
        status = AnalysisStatus.PENDING;
    }

    public enum AnalysisStatus {
        PENDING, COLLECTING, ANALYZING, GENERATING_IDEAS, COMPLETED, FAILED
    }
}
