package com.sentinelasm.scan;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/scans")
public class ScanController {
    private final ScanService scanService;

    public ScanController(ScanService scanService) {
        this.scanService = scanService;
    }

    @PostMapping
    public ScanDtos.ScanSummary create(@Valid @RequestBody ScanDtos.CreateScanRequest request) {
        return scanService.create(request);
    }

    @GetMapping
    public List<ScanDtos.ScanSummary> list() {
        return scanService.list();
    }

    @GetMapping("/{id}")
    public ScanDtos.ScanDetail get(@PathVariable UUID id) {
        return scanService.get(id);
    }
}
