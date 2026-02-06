package com.findcomplain.dto;

import lombok.Data;

@Data
public class IdeaGenerationResult {
    private String title;
    private String problem;
    private String solution;
    private String targetMarket;
    private String difficulty;
    private int potential;
    private String reasoning;
}
