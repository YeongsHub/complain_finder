package com.findcomplain.repository;

import com.findcomplain.domain.BusinessIdea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BusinessIdeaRepository extends JpaRepository<BusinessIdea, Long> {

    List<BusinessIdea> findByDifficulty(String difficulty);

    @Query("SELECT bi FROM BusinessIdea bi ORDER BY bi.potentialScore DESC")
    List<BusinessIdea> findTopByPotential();

    @Query("SELECT bi FROM BusinessIdea bi ORDER BY bi.createdAt DESC LIMIT :limit")
    List<BusinessIdea> findRecentIdeas(int limit);
}
