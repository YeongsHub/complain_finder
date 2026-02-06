package com.findcomplain.dto;

import lombok.Data;

import java.util.List;

@Data
public class ComplaintAnalysisResult {
    private boolean isComplaint;
    private String category;
    private int painLevel;
    private String coreProblem;
    private List<String> keywords;
}
