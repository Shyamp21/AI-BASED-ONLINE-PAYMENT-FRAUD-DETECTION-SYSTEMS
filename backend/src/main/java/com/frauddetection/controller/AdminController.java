package com.frauddetection.controller;

import com.frauddetection.dto.response.ModelMetricsResponse;
import com.frauddetection.dto.response.UserResponse;
import com.frauddetection.entity.AuditLog;
import com.frauddetection.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Administration", description = "Privileged administrator operations, user management, and ML performance metrics")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/users")
    @Operation(summary = "List all registered users and risk analysts")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @GetMapping("/model-metrics")
    @Operation(summary = "Retrieve real evaluation metrics for CatBoost, XGBoost, and Voting Ensemble")
    public ResponseEntity<ModelMetricsResponse> getModelMetrics() {
        return ResponseEntity.ok(adminService.getModelMetrics());
    }

    @GetMapping("/audit-logs")
    @Operation(summary = "View system security audit trail and user access history")
    public ResponseEntity<Page<AuditLog>> getAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(adminService.getAuditLogs(pageable));
    }
}
