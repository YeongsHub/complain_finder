package com.findcomplain.controller;

import com.findcomplain.domain.AnalysisSession;
import com.findcomplain.dto.AnalyzeRequest;
import com.findcomplain.dto.AnalyzeResponse;
import com.findcomplain.service.AnalysisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analyze")
@RequiredArgsConstructor
@Tag(name = "Analysis", description = "Reddit analysis endpoints")
public class AnalyzeController {

    private final AnalysisService analysisService;

    @PostMapping
    @Operation(summary = "Start a new analysis session")
    public ResponseEntity<AnalyzeResponse> startAnalysis(@Valid @RequestBody AnalyzeRequest request) {
        AnalysisSession session = analysisService.startAnalysis(request);

        return ResponseEntity.ok(AnalyzeResponse.builder()
                .sessionId(session.getId())
                .subreddit(session.getSubreddit())
                .status(session.getStatus())
                .message("Analysis started")
                .build());
    }

    @GetMapping("/{sessionId}/status")
    @Operation(summary = "Get analysis session status")
    public ResponseEntity<AnalyzeResponse> getStatus(@PathVariable Long sessionId) {
        AnalysisSession session = analysisService.getSession(sessionId);

        String message = switch (session.getStatus()) {
            case PENDING -> "Waiting to start...";
            case COLLECTING -> "Collecting Reddit posts...";
            case ANALYZING -> "Analyzing complaints with LLM...";
            case GENERATING_IDEAS -> "Generating business ideas...";
            case COMPLETED -> String.format("Completed! Found %d complaints from %d posts",
                    session.getTotalComplaints(), session.getTotalPosts());
            case FAILED -> "Analysis failed";
        };

        return ResponseEntity.ok(AnalyzeResponse.builder()
                .sessionId(session.getId())
                .subreddit(session.getSubreddit())
                .status(session.getStatus())
                .message(message)
                .build());
    }
}
