package com.frauddetection.service;

import com.frauddetection.dto.request.TransactionPredictRequest;
import com.frauddetection.dto.response.BatchPredictionResponse;
import com.frauddetection.dto.response.PredictionResponse;
import com.frauddetection.dto.response.TransactionDetailResponse;
import com.frauddetection.entity.FraudAlert;
import com.frauddetection.entity.PredictionResult;
import com.frauddetection.entity.Transaction;
import com.frauddetection.entity.User;
import com.frauddetection.exception.BadRequestException;
import com.frauddetection.exception.ResourceNotFoundException;
import com.frauddetection.repository.FraudAlertRepository;
import com.frauddetection.repository.PredictionResultRepository;
import com.frauddetection.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class TransactionService {

    private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);

    private final TransactionRepository transactionRepository;
    private final PredictionResultRepository predictionResultRepository;
    private final FraudAlertRepository fraudAlertRepository;
    private final MlClientService mlClientService;
    private final AuthService authService;
    private final AuditService auditService;

    public TransactionService(
            TransactionRepository transactionRepository,
            PredictionResultRepository predictionResultRepository,
            FraudAlertRepository fraudAlertRepository,
            MlClientService mlClientService,
            AuthService authService,
            AuditService auditService
    ) {
        this.transactionRepository = transactionRepository;
        this.predictionResultRepository = predictionResultRepository;
        this.fraudAlertRepository = fraudAlertRepository;
        this.mlClientService = mlClientService;
        this.authService = authService;
        this.auditService = auditService;
    }

    @Transactional
    public PredictionResponse processAndPredict(TransactionPredictRequest request, String ipAddress) {
        if (request.getExternalTransactionId() == null || request.getExternalTransactionId().isBlank()) {
            request.setExternalTransactionId("TXN-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }

        // Deduplication guard
        if (transactionRepository.existsByExternalTransactionId(request.getExternalTransactionId())) {
            throw new BadRequestException("Transaction with ID " + request.getExternalTransactionId() + " already exists");
        }

        User currentUser = authService.getCurrentAuthenticatedUser();

        // 1. Calculate domain error balances
        BigDecimal errOrig = request.getNewbalanceOrig().add(request.getAmount()).subtract(request.getOldbalanceOrg());
        BigDecimal errDest = request.getOldbalanceDest().add(request.getAmount()).subtract(request.getNewbalanceDest());

        // 2. Persist transaction entity
        Transaction txn = new Transaction();
        txn.setExternalTransactionId(request.getExternalTransactionId());
        txn.setTransactionAmount(request.getAmount());
        txn.setTransactionType(request.getType().toUpperCase());
        txn.setStep(request.getStep() != null ? request.getStep() : 1);
        txn.setSenderId(request.getSenderId());
        txn.setOldBalanceOrig(request.getOldbalanceOrg());
        txn.setNewBalanceOrig(request.getNewbalanceOrig());
        txn.setReceiverId(request.getReceiverId());
        txn.setOldBalanceDest(request.getOldbalanceDest());
        txn.setNewBalanceDest(request.getNewbalanceDest());
        txn.setErrorBalanceOrig(errOrig);
        txn.setErrorBalanceDest(errDest);
        txn.setTransactionTimestamp(LocalDateTime.now());
        txn.setCreatedBy(currentUser);

        Transaction savedTxn = transactionRepository.save(txn);

        // 3. Query ML Ensemble Microservice
        PredictionResponse prediction = mlClientService.predictSingle(request);
        prediction.setTransactionId(savedTxn.getId());

        // 4. Persist Prediction Result
        PredictionResult predResult = new PredictionResult();
        predResult.setTransaction(savedTxn);
        predResult.setModelVersion(prediction.getModelVersion());
        predResult.setPredictedClass(prediction.getPredictedClass());
        predResult.setFraudProbability(prediction.getFraudProbability());
        predResult.setRiskLevel(prediction.getRiskLevel());
        predResult.setCatboostProbability(prediction.getCatboostProbability());
        predResult.setXgboostProbability(prediction.getXgboostProbability());
        predResult.setEnsembleScore(prediction.getEnsembleScore());
        predResult.setExplanationSummary(prediction.getExplanationSummary());
        predResult.setPredictionTimestamp(LocalDateTime.now());

        PredictionResult savedPredResult = predictionResultRepository.save(predResult);

        // 5. Automatic Fraud Alert Generation
        if ("FRAUD".equalsIgnoreCase(prediction.getPredictedClass()) || "HIGH".equalsIgnoreCase(prediction.getRiskLevel())) {
            FraudAlert alert = new FraudAlert();
            alert.setPredictionResult(savedPredResult);
            alert.setReviewStatus("PENDING");
            alert.setPriority("HIGH".equalsIgnoreCase(prediction.getRiskLevel()) ? "CRITICAL" : "HIGH");
            fraudAlertRepository.save(alert);

            auditService.logAction(
                    currentUser != null ? currentUser.getId() : null,
                    currentUser != null ? currentUser.getUsername() : "ANONYMOUS",
                    "ALERT_GENERATED",
                    "FRAUD_ALERT",
                    String.valueOf(savedPredResult.getId()),
                    "Fraud suspected for " + savedTxn.getExternalTransactionId() + " (Prob: " + prediction.getFraudProbability() + ")",
                    ipAddress
            );
        }

        return prediction;
    }

    @Transactional
    public BatchPredictionResponse processBatchCsv(MultipartFile file, String ipAddress) {
        if (file.isEmpty()) {
            throw new BadRequestException("Uploaded CSV file is empty");
        }

        String filename = file.getOriginalFilename();
        if (filename == null || (!filename.endsWith(".csv") && !filename.endsWith(".txt"))) {
            throw new BadRequestException("Only CSV files are accepted");
        }

        List<TransactionPredictRequest> requests = new ArrayList<>();
        User currentUser = authService.getCurrentAuthenticatedUser();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new BadRequestException("CSV file has no header line");
            }

            String[] headers = headerLine.split(",");
            Map<String, Integer> colIndex = new HashMap<>();
            for (int i = 0; i < headers.length; i++) {
                colIndex.put(headers[i].trim().toLowerCase(), i);
            }

            // Verify essential PaySim columns
            if (!colIndex.containsKey("amount") || !colIndex.containsKey("type")) {
                throw new BadRequestException("CSV missing required columns 'type' and 'amount'");
            }

            String line;
            int rowNum = 1;
            while ((line = reader.readLine()) != null) {
                rowNum++;
                if (line.trim().isEmpty()) continue;

                String[] values = line.split(",");
                try {
                    TransactionPredictRequest req = new TransactionPredictRequest();
                    req.setExternalTransactionId("BATCH-" + System.currentTimeMillis() + "-" + rowNum);

                    req.setType(values[colIndex.get("type")].trim().toUpperCase());
                    req.setAmount(new BigDecimal(values[colIndex.get("amount")].trim()));

                    req.setStep(colIndex.containsKey("step") && colIndex.get("step") < values.length
                            ? Integer.parseInt(values[colIndex.get("step")].trim()) : 1);

                    req.setSenderId(colIndex.containsKey("nameorig") && colIndex.get("nameorig") < values.length
                            ? values[colIndex.get("nameorig")].trim() : "C_BATCH_" + rowNum);

                    req.setOldbalanceOrg(colIndex.containsKey("oldbalanceorg") && colIndex.get("oldbalanceorg") < values.length
                            ? new BigDecimal(values[colIndex.get("oldbalanceorg")].trim()) : BigDecimal.ZERO);

                    req.setNewbalanceOrig(colIndex.containsKey("newbalanceorig") && colIndex.get("newbalanceorig") < values.length
                            ? new BigDecimal(values[colIndex.get("newbalanceorig")].trim()) : BigDecimal.ZERO);

                    req.setReceiverId(colIndex.containsKey("namedest") && colIndex.get("namedest") < values.length
                            ? values[colIndex.get("namedest")].trim() : "M_BATCH_" + rowNum);

                    req.setOldbalanceDest(colIndex.containsKey("oldbalancedest") && colIndex.get("oldbalancedest") < values.length
                            ? new BigDecimal(values[colIndex.get("oldbalancedest")].trim()) : BigDecimal.ZERO);

                    req.setNewbalanceDest(colIndex.containsKey("newbalancedest") && colIndex.get("newbalancedest") < values.length
                            ? new BigDecimal(values[colIndex.get("newbalancedest")].trim()) : BigDecimal.ZERO);

                    requests.add(req);
                    if (requests.size() > 5000) {
                        break; // Safety limit
                    }
                } catch (Exception ex) {
                    logger.warn("Skipping malformed row {}: {}", rowNum, ex.getMessage());
                }
            }
        } catch (Exception ex) {
            throw new BadRequestException("Failed to parse CSV file: " + ex.getMessage());
        }

        if (requests.isEmpty()) {
            throw new BadRequestException("No valid transaction records found in CSV");
        }

        // Run batch inference
        List<PredictionResponse> predictions = mlClientService.predictBatch(requests);

        int fraudCount = 0;
        int legitCount = 0;

        for (int i = 0; i < requests.size(); i++) {
            TransactionPredictRequest req = requests.get(i);
            PredictionResponse pred = predictions.get(i);

            Transaction txn = new Transaction();
            txn.setExternalTransactionId(req.getExternalTransactionId());
            txn.setTransactionAmount(req.getAmount());
            txn.setTransactionType(req.getType());
            txn.setStep(req.getStep());
            txn.setSenderId(req.getSenderId());
            txn.setOldBalanceOrig(req.getOldbalanceOrg());
            txn.setNewBalanceOrig(req.getNewbalanceOrig());
            txn.setReceiverId(req.getReceiverId());
            txn.setOldBalanceDest(req.getOldbalanceDest());
            txn.setNewBalanceDest(req.getNewbalanceDest());
            txn.setCreatedBy(currentUser);
            Transaction savedTxn = transactionRepository.save(txn);

            pred.setTransactionId(savedTxn.getId());

            PredictionResult res = new PredictionResult();
            res.setTransaction(savedTxn);
            res.setModelVersion(pred.getModelVersion());
            res.setPredictedClass(pred.getPredictedClass());
            res.setFraudProbability(pred.getFraudProbability());
            res.setRiskLevel(pred.getRiskLevel());
            res.setCatboostProbability(pred.getCatboostProbability());
            res.setXgboostProbability(pred.getXgboostProbability());
            res.setEnsembleScore(pred.getEnsembleScore());
            res.setExplanationSummary(pred.getExplanationSummary());
            PredictionResult savedRes = predictionResultRepository.save(res);

            if ("FRAUD".equalsIgnoreCase(pred.getPredictedClass())) {
                fraudCount++;
                FraudAlert alert = new FraudAlert();
                alert.setPredictionResult(savedRes);
                alert.setReviewStatus("PENDING");
                alert.setPriority("HIGH");
                fraudAlertRepository.save(alert);
            } else {
                legitCount++;
            }
        }

        auditService.logAction(
                currentUser != null ? currentUser.getId() : null,
                currentUser != null ? currentUser.getUsername() : "ANONYMOUS",
                "BATCH_PREDICT_CSV",
                "BATCH_FILE",
                filename,
                "Processed " + requests.size() + " records (Fraud: " + fraudCount + ", Legit: " + legitCount + ")",
                ipAddress
        );

        return new BatchPredictionResponse(requests.size(), fraudCount, legitCount, predictions);
    }

    @Transactional(readOnly = true)
    public Page<TransactionDetailResponse> getTransactions(String query, String type, LocalDateTime start, LocalDateTime end, Pageable pageable) {
        Page<Transaction> page = transactionRepository.searchTransactions(
                (query != null && !query.isBlank()) ? query.trim() : null,
                (type != null && !type.isBlank()) ? type.trim().toUpperCase() : null,
                start,
                end,
                pageable
        );

        return page.map(t -> {
            TransactionDetailResponse res = new TransactionDetailResponse();
            res.setId(t.getId());
            res.setExternalTransactionId(t.getExternalTransactionId());
            res.setAmount(t.getTransactionAmount());
            res.setType(t.getTransactionType());
            res.setStep(t.getStep());
            res.setSenderId(t.getSenderId());
            res.setOldBalanceOrig(t.getOldBalanceOrig());
            res.setNewBalanceOrig(t.getNewBalanceOrig());
            res.setReceiverId(t.getReceiverId());
            res.setOldBalanceDest(t.getOldBalanceDest());
            res.setNewBalanceDest(t.getNewBalanceDest());
            res.setErrorBalanceOrig(t.getErrorBalanceOrig());
            res.setErrorBalanceDest(t.getErrorBalanceDest());
            res.setTimestamp(t.getTransactionTimestamp());

            predictionResultRepository.findByTransaction(t).ifPresent(p -> {
                res.setPredictedClass(p.getPredictedClass());
                res.setFraudProbability(p.getFraudProbability());
                res.setRiskLevel(p.getRiskLevel());
                res.setExplanationSummary(p.getExplanationSummary());
            });

            return res;
        });
    }

    @Transactional(readOnly = true)
    public TransactionDetailResponse getTransactionById(Long id) {
        Transaction t = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with ID: " + id));

        TransactionDetailResponse res = new TransactionDetailResponse();
        res.setId(t.getId());
        res.setExternalTransactionId(t.getExternalTransactionId());
        res.setAmount(t.getTransactionAmount());
        res.setType(t.getTransactionType());
        res.setStep(t.getStep());
        res.setSenderId(t.getSenderId());
        res.setOldBalanceOrig(t.getOldBalanceOrig());
        res.setNewBalanceOrig(t.getNewBalanceOrig());
        res.setReceiverId(t.getReceiverId());
        res.setOldBalanceDest(t.getOldBalanceDest());
        res.setNewBalanceDest(t.getNewBalanceDest());
        res.setErrorBalanceOrig(t.getErrorBalanceOrig());
        res.setErrorBalanceDest(t.getErrorBalanceDest());
        res.setTimestamp(t.getTransactionTimestamp());

        predictionResultRepository.findByTransaction(t).ifPresent(p -> {
            res.setPredictedClass(p.getPredictedClass());
            res.setFraudProbability(p.getFraudProbability());
            res.setRiskLevel(p.getRiskLevel());
            res.setExplanationSummary(p.getExplanationSummary());
        });

        return res;
    }
}
