package com.sentinelasm.scan;

import com.sentinelasm.domain.Priority;
import com.sentinelasm.domain.ScanStatus;
import com.sentinelasm.domain.ScanType;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class ScanDtos {
    public record CreateScanRequest(
            @NotBlank
            @Pattern(regexp = "^[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")
            String domain,
            ScanType scanType,
            Priority priority,
            @AssertTrue boolean authorized
    ) {}

    public record ScanSummary(
            UUID id,
            String domain,
            ScanType scanType,
            Priority priority,
            ScanStatus status,
            int progress,
            int riskScore,
            Instant createdAt,
            Instant completedAt
    ) {}

    public record ScanDetail(
            ScanSummary summary,
            List<String> logs,
            Object assets,
            Object findings
    ) {}
}
