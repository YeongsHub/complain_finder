package com.findcomplain.controller;

import com.findcomplain.domain.AppIdea;
import com.findcomplain.service.AppIdeaService;
import com.findcomplain.service.IdeaDiscoveryScheduler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/app-ideas")
@RequiredArgsConstructor
@Tag(name = "App Ideas", description = "App idea discovery and management")
public class AppIdeaController {

    private final AppIdeaService appIdeaService;
    private final IdeaDiscoveryScheduler scheduler;

    @GetMapping
    @Operation(summary = "Get all discovered app ideas")
    public ResponseEntity<List<AppIdea>> getAllIdeas() {
        return ResponseEntity.ok(appIdeaService.getAllIdeas());
    }

    @GetMapping("/top")
    @Operation(summary = "Get top rated app ideas")
    public ResponseEntity<List<AppIdea>> getTopIdeas() {
        return ResponseEntity.ok(appIdeaService.getTopIdeas());
    }

    @GetMapping("/bookmarked")
    @Operation(summary = "Get bookmarked ideas")
    public ResponseEntity<List<AppIdea>> getBookmarkedIdeas() {
        return ResponseEntity.ok(appIdeaService.getBookmarkedIdeas());
    }

    @PostMapping("/{id}/bookmark")
    @Operation(summary = "Toggle bookmark status")
    public ResponseEntity<AppIdea> toggleBookmark(@PathVariable Long id) {
        return ResponseEntity.ok(appIdeaService.toggleBookmark(id));
    }

    @PostMapping("/discover")
    @Operation(summary = "Manually trigger idea discovery")
    public ResponseEntity<Map<String, Object>> triggerDiscovery() {
        int count = scheduler.runDiscoveryNow();
        return ResponseEntity.ok(Map.of(
                "message", "Discovery completed",
                "newIdeasFound", count,
                "subredditsScanned", scheduler.getTargetSubreddits()
        ));
    }

    @PostMapping("/analyze/{subreddit}")
    @Operation(summary = "Analyze specific subreddit for ideas")
    public ResponseEntity<List<AppIdea>> analyzeSubreddit(
            @PathVariable String subreddit,
            @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(appIdeaService.analyzeSubreddit(subreddit, limit));
    }

    @GetMapping("/subreddits")
    @Operation(summary = "Get list of target subreddits")
    public ResponseEntity<Map<String, Object>> getTargetSubreddits() {
        return ResponseEntity.ok(Map.of(
                "defaults", scheduler.getDefaultSubreddits(),
                "custom", scheduler.getCustomSubreddits(),
                "all", scheduler.getTargetSubreddits()
        ));
    }

    @PostMapping("/subreddits")
    @Operation(summary = "Add a custom subreddit")
    public ResponseEntity<Map<String, Object>> addCustomSubreddit(@RequestBody Map<String, String> body) {
        String subreddit = body.get("subreddit");
        String added = scheduler.addCustomSubreddit(subreddit);
        return ResponseEntity.ok(Map.of(
                "message", "Added r/" + added,
                "defaults", scheduler.getDefaultSubreddits(),
                "custom", scheduler.getCustomSubreddits(),
                "all", scheduler.getTargetSubreddits()
        ));
    }

    @DeleteMapping("/subreddits/{subreddit}")
    @Operation(summary = "Remove a custom subreddit")
    public ResponseEntity<Map<String, Object>> removeCustomSubreddit(@PathVariable String subreddit) {
        scheduler.removeCustomSubreddit(subreddit);
        return ResponseEntity.ok(Map.of(
                "message", "Removed r/" + subreddit,
                "defaults", scheduler.getDefaultSubreddits(),
                "custom", scheduler.getCustomSubreddits(),
                "all", scheduler.getTargetSubreddits()
        ));
    }
}
