package com.frauddetection.dto.request;

import jakarta.validation.constraints.NotBlank;

public class AlertStatusUpdateRequest {

    @NotBlank(message = "Review status is required (PENDING, REVIEWED, RESOLVED)")
    private String status;

    private String remarks;

    public AlertStatusUpdateRequest() {}

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}
