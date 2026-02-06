package com.findcomplain.repository;

import com.findcomplain.domain.Complaint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, Long> {

    List<Complaint> findBySubreddit(String subreddit);

    List<Complaint> findByCategory(String category);

    List<Complaint> findBySubredditAndCategory(String subreddit, String category);

    @Query("SELECT c FROM Complaint c WHERE c.subreddit = :subreddit ORDER BY c.painLevel DESC")
    List<Complaint> findTopPainPointsBySubreddit(String subreddit);

    @Query("SELECT c.category, COUNT(c) FROM Complaint c WHERE c.subreddit = :subreddit GROUP BY c.category")
    List<Object[]> countByCategory(String subreddit);

    @Query("SELECT DISTINCT c.subreddit FROM Complaint c")
    List<String> findDistinctSubreddits();

    boolean existsByRedditPostId(String redditPostId);
}
