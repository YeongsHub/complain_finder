package com.findcomplain.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AppIdeaAnalysisResult {
    @JsonAlias({"isViable", "is_viable"})
    private boolean viable;

    @JsonAlias({"appName", "app_name"})
    private String appName;

    @JsonAlias({"problemSummary", "problem_summary"})
    private String problemSummary;

    @JsonAlias({"proposedSolution", "proposed_solution"})
    private String proposedSolution;

    @JsonAlias({"targetUsers", "target_users"})
    private String targetUsers;

    @JsonAlias({"keyFeatures", "key_features"})
    private String keyFeatures;

    @JsonAlias({"techStack", "tech_stack"})
    private String techStack;

    private String difficulty;

    @JsonAlias({"viabilityScore", "viability_score"})
    private int viabilityScore;

    private String reasoning;
}
