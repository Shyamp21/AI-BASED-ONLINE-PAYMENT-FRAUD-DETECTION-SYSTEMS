package com.frauddetection.service;

import com.frauddetection.dto.response.DashboardStatisticsResponse;
import com.frauddetection.dto.response.DashboardSummaryResponse;
import com.frauddetection.dto.response.TransactionDetailResponse;
import com.frauddetection.entity.Transaction;
import com.frauddetection.repository.FraudAlertRepository;
import com.frauddetection.repository.PredictionResultRepository;
import com.frauddetection.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class DashboardService {

    private final TransactionRepository transactionRepository;
    private final PredictionResultRepository predictionResultRepository;
    private final FraudAlertRepository fraudAlertRepository;

    public DashboardService(
            TransactionRepository transactionRepository,
            PredictionResultRepository predictionResultRepository,
            FraudAlertRepository fraudAlertRepository
    ) {
        this.transactionRepository = transactionRepository;
        this.predictionResultRepository = predictionResultRepository;
        this.fraudAlertRepository = fraudAlertRepository;
    }

    @Transactional(readOnly = true)
    public DashboardSummaryResponse getSummary() {
        long totalTransactions = transactionRepository.count();
        long fraudCount = predictionResultRepository.countByPredictedClass("FRAUD");
        long legitCount = predictionResultRepository.countByPredictedClass("LEGITIMATE");
        long pendingAlerts = fraudAlertRepository.countByReviewStatus("PENDING");

        double fraudPct = totalTransactions > 0 ? ((double) fraudCount / totalTransactions) * 100.0 : 0.0;

        List<Transaction> allTxns = transactionRepository.findAll();
        BigDecimal totalAmount = allTxns.stream()
                .map(Transaction::getTransactionAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new DashboardSummaryResponse(
                totalTransactions,
                fraudCount,
                legitCount,
                Math.round(fraudPct * 100.0) / 100.0,
                pendingAlerts,
                totalAmount.doubleValue()
        );
    }

    @Transactional(readOnly = true)
    public DashboardStatisticsResponse getStatistics() {
        DashboardStatisticsResponse response = new DashboardStatisticsResponse();

        // 1. Transaction Types Distribution
        Map<String, Long> typeDist = new HashMap<>();
        List<Object[]> typeCounts = transactionRepository.countTransactionsByType();
        for (Object[] row : typeCounts) {
            typeDist.put((String) row[0], ((Number) row[1]).longValue());
        }
        response.setTransactionTypeDistribution(typeDist);

        // 2. Risk Level Distribution
        Map<String, Long> riskDist = new HashMap<>();
        List<Object[]> riskCounts = predictionResultRepository.countByRiskLevelGroup();
        for (Object[] row : riskCounts) {
            riskDist.put((String) row[0], ((Number) row[1]).longValue());
        }
        response.setRiskLevelDistribution(riskDist);

        // 3. Predicted Class Distribution
        Map<String, Long> classDist = new HashMap<>();
        List<Object[]> classCounts = predictionResultRepository.countByPredictedClassGroup();
        for (Object[] row : classCounts) {
            classDist.put((String) row[0], ((Number) row[1]).longValue());
        }
        response.setPredictedClassDistribution(classDist);

        // 4. Daily Trends (Past 7 days)
        List<Transaction> recent = transactionRepository.findAll();
        Map<String, int[]> dailyMap = new LinkedHashMap<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (Transaction t : recent) {
            if (t.getTransactionTimestamp() != null) {
                String day = t.getTransactionTimestamp().format(fmt);
                dailyMap.putIfAbsent(day, new int[]{0, 0}); // [legit, fraud]
                predictionResultRepository.findByTransaction(t).ifPresent(p -> {
                    if ("FRAUD".equalsIgnoreCase(p.getPredictedClass())) {
                        dailyMap.get(day)[1]++;
                    } else {
                        dailyMap.get(day)[0]++;
                    }
                });
            }
        }

        List<Map<String, Object>> trendList = new ArrayList<>();
        dailyMap.forEach((date, counts) -> {
            Map<String, Object> map = new HashMap<>();
            map.put("date", date);
            map.put("legitimate", counts[0]);
            map.put("fraud", counts[1]);
            trendList.add(map);
        });
        response.setDailyTrends(trendList);

        // 5. Recent Activity (Top 10)
        List<Transaction> top10 = transactionRepository.findTop10ByOrderByTransactionTimestampDesc();
        List<TransactionDetailResponse> recentList = new ArrayList<>();
        for (Transaction t : top10) {
            TransactionDetailResponse d = new TransactionDetailResponse();
            d.setId(t.getId());
            d.setExternalTransactionId(t.getExternalTransactionId());
            d.setAmount(t.getTransactionAmount());
            d.setType(t.getTransactionType());
            d.setSenderId(t.getSenderId());
            d.setReceiverId(t.getReceiverId());
            d.setTimestamp(t.getTransactionTimestamp());
            predictionResultRepository.findByTransaction(t).ifPresent(p -> {
                d.setPredictedClass(p.getPredictedClass());
                d.setFraudProbability(p.getFraudProbability());
                d.setRiskLevel(p.getRiskLevel());
                d.setExplanationSummary(p.getExplanationSummary());
            });
            recentList.add(d);
        }
        response.setRecentActivity(recentList);

        return response;
    }
}
