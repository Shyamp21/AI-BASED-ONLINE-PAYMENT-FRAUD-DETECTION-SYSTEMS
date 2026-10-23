package com.frauddetection.controller;

import com.frauddetection.dto.request.TransactionPredictRequest;
import com.frauddetection.dto.response.BatchPredictionResponse;
import com.frauddetection.dto.response.PredictionResponse;
import com.frauddetection.dto.response.TransactionDetailResponse;
import com.frauddetection.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/transactions")
@Tag(name = "Transactions & Predictions", description = "Transaction submission, real-time ML fraud prediction, batch CSV processing, and query history")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/predict")
    @Operation(summary = "Analyze a single transaction through the ensemble ML model")
    public ResponseEntity<PredictionResponse> predict(
            @Valid @RequestBody TransactionPredictRequest request,
            HttpServletRequest servletRequest
    ) {
        String clientIp = servletRequest.getRemoteAddr();
        PredictionResponse response = transactionService.processAndPredict(request, clientIp);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping(value = "/batch-predict", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload CSV transaction file for batch ensemble inference")
    public ResponseEntity<BatchPredictionResponse> batchPredict(
            @RequestParam("file") MultipartFile file,
            HttpServletRequest servletRequest
    ) {
        String clientIp = servletRequest.getRemoteAddr();
        BatchPredictionResponse response = transactionService.processBatchCsv(file, clientIp);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Search and filter transaction history with pagination and sorting")
    public ResponseEntity<Page<TransactionDetailResponse>> getTransactions(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(defaultValue = "transactionTimestamp") String sortBy,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(transactionService.getTransactions(query, type, startDate, endDate, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get full details of a specific transaction and its prediction result")
    public ResponseEntity<TransactionDetailResponse> getTransactionById(@PathVariable Long id) {
        return ResponseEntity.ok(transactionService.getTransactionById(id));
    }
}
