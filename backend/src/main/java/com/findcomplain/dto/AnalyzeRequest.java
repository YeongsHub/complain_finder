package com.findcomplain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class AnalyzeRequest {
    @NotBlank(message = "Subreddit is required")
    private String subreddit;
    private List<String> keywords;
    private Integer limit = 50;
}
