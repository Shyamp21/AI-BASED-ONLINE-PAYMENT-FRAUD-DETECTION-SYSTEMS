package com.frauddetection.controller;

import com.frauddetection.dto.request.AlertStatusUpdateRequest;
import com.frauddetection.dto.response.AlertResponse;
import com.frauddetection.service.AlertService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/alerts")
@Tag(name = "Fraud Alerts", description = "Monitor, review, and investigate suspected fraudulent payment alerts")
public class AlertController {

    private final AlertService alertService;

    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    @GetMapping
    @Operation(summary = "Get list of fraud alerts filtered by review status with pagination")
    public ResponseEntity<Page<AlertResponse>> getAlerts(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(alertService.getAlerts(status, pageable));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update fraud alert review status (PENDING, REVIEWED, RESOLVED)")
    public ResponseEntity<AlertResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody AlertStatusUpdateRequest request,
            HttpServletRequest servletRequest
    ) {
        String clientIp = servletRequest.getRemoteAddr();
        AlertResponse response = alertService.updateAlertStatus(id, request, clientIp);
        return ResponseEntity.ok(response);
    }
}
