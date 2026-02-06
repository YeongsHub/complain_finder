package com.findcomplain.repository;

import com.findcomplain.domain.AnalysisSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnalysisSessionRepository extends JpaRepository<AnalysisSession, Long> {

    List<AnalysisSession> findByStatus(AnalysisSession.AnalysisStatus status);

    List<AnalysisSession> findBySubreddit(String subreddit);
}
