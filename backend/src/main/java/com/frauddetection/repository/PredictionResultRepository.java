package com.frauddetection.repository;

import com.frauddetection.entity.PredictionResult;
import com.frauddetection.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PredictionResultRepository extends JpaRepository<PredictionResult, Long> {
    Optional<PredictionResult> findByTransaction(Transaction transaction);
    Optional<PredictionResult> findByTransactionId(Long transactionId);

    long countByPredictedClass(String predictedClass);
    long countByRiskLevel(String riskLevel);

    @Query("SELECT p.riskLevel, COUNT(p) FROM PredictionResult p GROUP BY p.riskLevel")
    List<Object[]> countByRiskLevelGroup();

    @Query("SELECT p.predictedClass, COUNT(p) FROM PredictionResult p GROUP BY p.predictedClass")
    List<Object[]> countByPredictedClassGroup();
}
