package com.sentinelasm.scan;

import com.sentinelasm.asset.Asset;
import com.sentinelasm.asset.AssetRepository;
import com.sentinelasm.domain.Priority;
import com.sentinelasm.domain.ScanStatus;
import com.sentinelasm.domain.ScanType;
import com.sentinelasm.finding.Finding;
import com.sentinelasm.finding.FindingRepository;
import com.sentinelasm.notification.NotificationService;
import com.sentinelasm.port.OpenPort;
import com.sentinelasm.port.OpenPortRepository;
import com.sentinelasm.risk.RiskService;
import com.sentinelasm.scanner.ReconEngine;
import com.sentinelasm.scanner.ScanResult;
import com.sentinelasm.user.AppUser;
import com.sentinelasm.user.UserService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.io.PrintWriter;
import java.io.StringWriter;

@Service
public class ScanService {
    private final ScanRepository scans;
    private final AssetRepository assets;
    private final FindingRepository findings;
    private final OpenPortRepository ports;
    private final UserService userService;
    private final ReconEngine reconEngine;
    private final RiskService riskService;
    private final NotificationService notifications;

    public ScanService(ScanRepository scans, AssetRepository assets, FindingRepository findings, OpenPortRepository ports, UserService userService,
                       ReconEngine reconEngine, RiskService riskService, NotificationService notifications) {
        this.scans = scans;
        this.assets = assets;
        this.findings = findings;
        this.ports = ports;
        this.userService = userService;
        this.reconEngine = reconEngine;
        this.riskService = riskService;
        this.notifications = notifications;
    }

    @Transactional
    public ScanDtos.ScanSummary create(ScanDtos.CreateScanRequest request) {
        AppUser owner = userService.currentUser();
        Scan scan = new Scan();
        scan.setOwner(owner);
        scan.setDomain(request.domain().toLowerCase());
        scan.setScanType(request.scanType() == null ? ScanType.STANDARD : request.scanType());
        scan.setPriority(request.priority() == null ? Priority.NORMAL : request.priority());
        scan.getLogs().add("Queued scan job for " + scan.getDomain());
        scans.save(scan);
        runAsync(scan.getId());
        return summary(scan);
    }

    public List<ScanDtos.ScanSummary> list() {
        return scans.findByOwnerOrderByCreatedAtDesc(userService.currentUser()).stream().map(this::summary).toList();
    }

    public ScanDtos.ScanDetail get(UUID id) {
        AppUser owner = userService.currentUser();
        Scan scan = scans.findByIdAndOwner(id, owner).orElseThrow(() -> new IllegalArgumentException("Scan not found"));
        return new ScanDtos.ScanDetail(summary(scan), scan.getLogs(), assets.findByScan(scan), findings.findByScan(scan));
    }

    @Async("scanExecutor")
    public void runAsync(UUID scanId) {
        Scan scan = scans.findById(scanId).orElseThrow();
        try {
            update(scan, ScanStatus.RUNNING, 5, "Initializing Scan...");
            scan.setStartedAt(Instant.now());
            ScanResult result = reconEngine.run(scan.getDomain(), line -> append(scanId, line));
            persistResults(scanId, result);
        } catch (Exception error) {
            Scan failed = scans.findById(scanId).orElseThrow();
            failed.setStatus(ScanStatus.FAILED);
            failed.setFailureReason(error.getMessage());
            failed.setCompletedAt(Instant.now());
            failed.getLogs().add("Failed: " + error.getMessage());
            failed.getLogs().add(stackTrace(error));
            scans.save(failed);
        }
    }

    @Transactional
    public void persistResults(UUID scanId, ScanResult result) {
        Scan scan = scans.findById(scanId).orElseThrow();
        scan.setProgress(75);
        scans.save(scan);

        for (ScanResult.AssetFinding item : result.assets) {
            Asset asset = new Asset();
            asset.setOwner(scan.getOwner());
            asset.setScan(scan);
            asset.setRootDomain(scan.getDomain());
            asset.setSubdomain(item.subdomain());
            asset.setIpAddress(item.ipAddress());
            asset.setStatus(item.status());
            asset.setResponseCode(item.responseCode());
            asset.setPageTitle(item.pageTitle());
            asset.setTechnologies(item.technologies());
            asset.setScreenshotPath(item.screenshotPath());
            Asset savedAsset = assets.save(asset);

            for (ScanResult.PortFinding itemPort : item.ports()) {
                OpenPort port = new OpenPort();
                port.setOwner(scan.getOwner());
                port.setAsset(savedAsset);
                port.setPort(itemPort.port());
                port.setProtocol(itemPort.protocol());
                port.setService(itemPort.service());
                port.setVersion(itemPort.version());
                port.setState(itemPort.state());
                ports.save(port);
            }
        }

        for (ScanResult.VulnerabilityFinding item : result.findings) {
            Finding finding = new Finding();
            finding.setOwner(scan.getOwner());
            finding.setScan(scan);
            finding.setSeverity(item.severity());
            finding.setName(item.name());
            finding.setDescription(item.description());
            finding.setAffectedUrl(item.affectedUrl());
            finding.setMatchedTemplate(item.template());
            finding.setCve(item.cve());
            finding.setCvss(item.cvss());
            finding.setRecommendation(item.recommendation());
            findings.save(finding);
        }

        List<Finding> savedFindings = findings.findByScan(scan);
        scan.setRiskScore(riskService.score(savedFindings));
        scan.setStatus(ScanStatus.COMPLETED);
        scan.setProgress(100);
        scan.setCompletedAt(Instant.now());
        scan.getLogs().add("Completed.");
        scans.save(scan);

        notifications.create(scan.getOwner(), "Scan completed", scan.getDomain() + " completed with risk score " + scan.getRiskScore(), "scan");
        if (savedFindings.stream().anyMatch(f -> f.getSeverity().name().equals("CRITICAL"))) {
            notifications.create(scan.getOwner(), "Critical finding detected", "A critical finding was discovered on " + scan.getDomain(), "critical");
        }
    }

    private void update(Scan scan, ScanStatus status, int progress, String log) {
        scan.setStatus(status);
        scan.setProgress(progress);
        scan.getLogs().add(log);
        scans.save(scan);
    }

    private void append(UUID scanId, String line) {
        Scan scan = scans.findById(scanId).orElseThrow();
        scan.getLogs().add(line);
        scan.setProgress(Math.min(95, scan.getProgress() + 7));
        scans.save(scan);
    }

    private ScanDtos.ScanSummary summary(Scan scan) {
        return new ScanDtos.ScanSummary(scan.getId(), scan.getDomain(), scan.getScanType(), scan.getPriority(),
                scan.getStatus(), scan.getProgress(), scan.getRiskScore(), scan.getCreatedAt(), scan.getCompletedAt());
    }

    private String stackTrace(Exception error) {
        StringWriter writer = new StringWriter();
        error.printStackTrace(new PrintWriter(writer));
        return writer.toString();
    }
}
