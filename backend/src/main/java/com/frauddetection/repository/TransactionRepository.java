package com.frauddetection.repository;

import com.frauddetection.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Optional<Transaction> findByExternalTransactionId(String externalTransactionId);
    boolean existsByExternalTransactionId(String externalTransactionId);

    @Query("SELECT t FROM Transaction t WHERE " +
           "(:query IS NULL OR LOWER(t.externalTransactionId) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(t.senderId) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(t.receiverId) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
           "(:type IS NULL OR t.transactionType = :type) AND " +
           "(:startDate IS NULL OR t.transactionTimestamp >= :startDate) AND " +
           "(:endDate IS NULL OR t.transactionTimestamp <= :endDate)")
    Page<Transaction> searchTransactions(
            @Param("query") String query,
            @Param("type") String type,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );

    @Query("SELECT t.transactionType, COUNT(t) FROM Transaction t GROUP BY t.transactionType")
    List<Object[]> countTransactionsByType();

    List<Transaction> findTop10ByOrderByTransactionTimestampDesc();
}
