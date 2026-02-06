package com.findcomplain.service;

import com.findcomplain.domain.AnalysisSession;
import com.findcomplain.domain.BusinessIdea;
import com.findcomplain.domain.Complaint;
import com.findcomplain.dto.AnalyzeRequest;
import com.findcomplain.dto.RedditPost;
import com.findcomplain.repository.AnalysisSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalysisService {

    private final RedditCrawlerService redditCrawler;
    private final ComplaintAnalyzerService complaintAnalyzer;
    private final IdeaGeneratorService ideaGenerator;
    private final AnalysisSessionRepository sessionRepository;

    @Transactional
    public AnalysisSession startAnalysis(AnalyzeRequest request) {
        AnalysisSession session = AnalysisSession.builder()
                .subreddit(request.getSubreddit())
                .keywords(request.getKeywords())
                .totalPosts(0)
                .totalComplaints(0)
                .build();

        session = sessionRepository.save(session);
        log.info("Created analysis session {} for r/{}", session.getId(), request.getSubreddit());

        runAnalysisAsync(session.getId(), request);

        return session;
    }

    @Async
    @Transactional
    public void runAnalysisAsync(Long sessionId, AnalyzeRequest request) {
        AnalysisSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found: " + sessionId));

        try {
            // Step 1: Collect posts
            updateSessionStatus(session, AnalysisSession.AnalysisStatus.COLLECTING);
            List<RedditPost> posts = redditCrawler.fetchPosts(
                    request.getSubreddit(),
                    request.getKeywords(),
                    request.getLimit()
            );
            session.setTotalPosts(posts.size());
            sessionRepository.save(session);

            // Step 2: Analyze complaints
            updateSessionStatus(session, AnalysisSession.AnalysisStatus.ANALYZING);
            List<Complaint> complaints = complaintAnalyzer.analyzeAndSavePosts(posts);
            session.setTotalComplaints(complaints.size());
            sessionRepository.save(session);

            // Step 3: Generate ideas
            updateSessionStatus(session, AnalysisSession.AnalysisStatus.GENERATING_IDEAS);
            List<BusinessIdea> ideas = ideaGenerator.generateIdeas(complaints, request.getSubreddit());

            // Complete
            session.setStatus(AnalysisSession.AnalysisStatus.COMPLETED);
            session.setCompletedAt(LocalDateTime.now());
            sessionRepository.save(session);

            log.info("Analysis session {} completed: {} posts, {} complaints, {} ideas",
                    sessionId, posts.size(), complaints.size(), ideas.size());

        } catch (Exception e) {
            log.error("Analysis session {} failed", sessionId, e);
            session.setStatus(AnalysisSession.AnalysisStatus.FAILED);
            session.setCompletedAt(LocalDateTime.now());
            sessionRepository.save(session);
        }
    }

    private void updateSessionStatus(AnalysisSession session, AnalysisSession.AnalysisStatus status) {
        session.setStatus(status);
        sessionRepository.save(session);
        log.info("Session {} status updated to {}", session.getId(), status);
    }

    public AnalysisSession getSession(Long sessionId) {
        return sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found: " + sessionId));
    }
}
