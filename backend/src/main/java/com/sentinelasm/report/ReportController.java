package com.sentinelasm.report;

import com.sentinelasm.scan.ScanDtos;
import com.sentinelasm.scan.ScanService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/reports")
public class ReportController {
    private final ScanService scans;

    public ReportController(ScanService scans) {
        this.scans = scans;
    }

    @GetMapping("/{scanId}.json")
    public ScanDtos.ScanDetail json(@PathVariable UUID scanId) {
        return scans.get(scanId);
    }

    @GetMapping("/{scanId}.csv")
    public ResponseEntity<String> csv(@PathVariable UUID scanId) {
        ScanDtos.ScanDetail detail = scans.get(scanId);
        String csv = "section,value\nscan," + detail.summary().domain() + "\nrisk," + detail.summary().riskScore() + "\n";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=asm-report-" + scanId + ".csv")
                .contentType(MediaType.TEXT_PLAIN)
                .body(csv);
    }
}
