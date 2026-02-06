package com.findcomplain.dto;

import com.findcomplain.domain.AnalysisSession;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AnalyzeResponse {
    private Long sessionId;
    private String subreddit;
    private AnalysisSession.AnalysisStatus status;
    private String message;
}
