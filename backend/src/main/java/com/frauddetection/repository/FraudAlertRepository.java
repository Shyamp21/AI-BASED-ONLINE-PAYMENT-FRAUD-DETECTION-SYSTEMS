package com.frauddetection.repository;

import com.frauddetection.entity.FraudAlert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FraudAlertRepository extends JpaRepository<FraudAlert, Long> {
    long countByReviewStatus(String reviewStatus);

    @Query("SELECT a FROM FraudAlert a JOIN FETCH a.predictionResult pr JOIN FETCH pr.transaction t WHERE " +
           "(:status IS NULL OR a.reviewStatus = :status) ORDER BY a.createdAt DESC")
    Page<FraudAlert> findAlertsWithDetails(@Param("status") String status, Pageable pageable);

    List<FraudAlert> findTop5ByOrderByCreatedAtDesc();
}
