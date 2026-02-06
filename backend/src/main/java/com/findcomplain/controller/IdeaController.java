package com.findcomplain.controller;

import com.findcomplain.domain.BusinessIdea;
import com.findcomplain.repository.BusinessIdeaRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ideas")
@RequiredArgsConstructor
@Tag(name = "Ideas", description = "Business idea management endpoints")
public class IdeaController {

    private final BusinessIdeaRepository ideaRepository;

    @GetMapping
    @Operation(summary = "Get all business ideas")
    public ResponseEntity<List<BusinessIdea>> getIdeas(
            @RequestParam(required = false) String difficulty,
            @RequestParam(defaultValue = "100") int limit) {

        List<BusinessIdea> ideas;

        if (difficulty != null) {
            ideas = ideaRepository.findByDifficulty(difficulty);
        } else {
            ideas = ideaRepository.findRecentIdeas(limit);
        }

        return ResponseEntity.ok(ideas);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get an idea by ID")
    public ResponseEntity<BusinessIdea> getIdea(@PathVariable Long id) {
        return ideaRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/top")
    @Operation(summary = "Get top ideas by potential score")
    public ResponseEntity<List<BusinessIdea>> getTopIdeas() {
        return ResponseEntity.ok(ideaRepository.findTopByPotential());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an idea")
    public ResponseEntity<Void> deleteIdea(@PathVariable Long id) {
        if (ideaRepository.existsById(id)) {
            ideaRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
