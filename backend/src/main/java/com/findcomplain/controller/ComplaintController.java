package com.findcomplain.controller;

import com.findcomplain.domain.Complaint;
import com.findcomplain.repository.ComplaintRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/complaints")
@RequiredArgsConstructor
@Tag(name = "Complaints", description = "Complaint management endpoints")
public class ComplaintController {

    private final ComplaintRepository complaintRepository;

    @GetMapping
    @Operation(summary = "Get all complaints with optional filters")
    public ResponseEntity<List<Complaint>> getComplaints(
            @RequestParam(required = false) String subreddit,
            @RequestParam(required = false) String category) {

        List<Complaint> complaints;

        if (subreddit != null && category != null) {
            complaints = complaintRepository.findBySubredditAndCategory(subreddit, category);
        } else if (subreddit != null) {
            complaints = complaintRepository.findBySubreddit(subreddit);
        } else if (category != null) {
            complaints = complaintRepository.findByCategory(category);
        } else {
            complaints = complaintRepository.findAll();
        }

        return ResponseEntity.ok(complaints);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a complaint by ID")
    public ResponseEntity<Complaint> getComplaint(@PathVariable Long id) {
        return complaintRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/subreddits")
    @Operation(summary = "Get list of analyzed subreddits")
    public ResponseEntity<List<String>> getSubreddits() {
        return ResponseEntity.ok(complaintRepository.findDistinctSubreddits());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a complaint")
    public ResponseEntity<Void> deleteComplaint(@PathVariable Long id) {
        if (complaintRepository.existsById(id)) {
            complaintRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
