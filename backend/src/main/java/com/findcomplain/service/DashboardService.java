package com.findcomplain.service;

import com.findcomplain.domain.BusinessIdea;
import com.findcomplain.domain.Complaint;
import com.findcomplain.dto.DashboardStats;
import com.findcomplain.repository.BusinessIdeaRepository;
import com.findcomplain.repository.ComplaintRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ComplaintRepository complaintRepository;
    private final BusinessIdeaRepository ideaRepository;

    public DashboardStats getStats() {
        List<Complaint> complaints = complaintRepository.findAll();
        List<BusinessIdea> ideas = ideaRepository.findAll();

        Map<String, Long> categoryDistribution = complaints.stream()
                .filter(c -> c.getCategory() != null)
                .collect(Collectors.groupingBy(Complaint::getCategory, Collectors.counting()));

        List<DashboardStats.RecentActivity> activities = new ArrayList<>();

        complaints.stream()
                .sorted(Comparator.comparing(Complaint::getAnalyzedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(5)
                .forEach(c -> activities.add(DashboardStats.RecentActivity.builder()
                        .type("complaint")
                        .description("New complaint from r/" + c.getSubreddit() + ": " + truncate(c.getTitle(), 50))
                        .timestamp(c.getAnalyzedAt() != null ?
                                c.getAnalyzedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : "")
                        .build()));

        ideas.stream()
                .sorted(Comparator.comparing(BusinessIdea::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(5)
                .forEach(i -> activities.add(DashboardStats.RecentActivity.builder()
                        .type("idea")
                        .description("New idea generated: " + truncate(i.getTitle(), 50))
                        .timestamp(i.getCreatedAt() != null ?
                                i.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : "")
                        .build()));

        activities.sort((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()));

        return DashboardStats.builder()
                .totalComplaints(complaints.size())
                .totalIdeas(ideas.size())
                .totalSubreddits(complaintRepository.findDistinctSubreddits().size())
                .categoryDistribution(categoryDistribution)
                .recentActivities(activities.stream().limit(10).collect(Collectors.toList()))
                .build();
    }

    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        return text.length() > maxLength ? text.substring(0, maxLength) + "..." : text;
    }
}
