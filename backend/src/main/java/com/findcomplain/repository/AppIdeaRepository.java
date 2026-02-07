package com.findcomplain.repository;

import com.findcomplain.domain.AppIdea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppIdeaRepository extends JpaRepository<AppIdea, Long> {

    boolean existsByRedditPostId(String redditPostId);

    List<AppIdea> findBySubreddit(String subreddit);

    List<AppIdea> findByBookmarkedTrue();

    @Query("SELECT a FROM AppIdea a ORDER BY a.viabilityScore DESC")
    List<AppIdea> findTopByViability();

    @Query("SELECT a FROM AppIdea a ORDER BY a.analyzedAt DESC")
    List<AppIdea> findRecentIdeas();

    @Query("SELECT a FROM AppIdea a WHERE a.difficulty = :difficulty ORDER BY a.viabilityScore DESC")
    List<AppIdea> findByDifficultyOrderByViability(String difficulty);
}
