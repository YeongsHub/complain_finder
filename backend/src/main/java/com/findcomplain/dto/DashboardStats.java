package com.findcomplain.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class DashboardStats {
    private long totalComplaints;
    private long totalIdeas;
    private long totalSubreddits;
    private Map<String, Long> categoryDistribution;
    private List<RecentActivity> recentActivities;

    @Data
    @Builder
    public static class RecentActivity {
        private String type;
        private String description;
        private String timestamp;
    }
}
