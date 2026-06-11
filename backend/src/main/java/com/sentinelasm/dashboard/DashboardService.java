package com.sentinelasm.dashboard;

import com.sentinelasm.asset.Asset;
import com.sentinelasm.asset.AssetRepository;
import com.sentinelasm.domain.ScanStatus;
import com.sentinelasm.domain.Severity;
import com.sentinelasm.finding.FindingRepository;
import com.sentinelasm.scan.Scan;
import com.sentinelasm.scan.ScanRepository;
import com.sentinelasm.user.AppUser;
import com.sentinelasm.user.UserService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardService {
    private final ScanRepository scans;
    private final AssetRepository assets;
    private final FindingRepository findings;
    private final UserService userService;

    public DashboardService(ScanRepository scans, AssetRepository assets, FindingRepository findings, UserService userService) {
        this.scans = scans;
        this.assets = assets;
        this.findings = findings;
        this.userService = userService;
    }

    public DashboardDtos.Stats stats() {
        AppUser user = userService.currentUser();
        List<Scan> userScans = scans.findByOwnerOrderByCreatedAtDesc(user);
        List<Asset> userAssets = assets.findByOwnerOrderByLastSeenDesc(user);
        long total = scans.countByOwner(user);
        long completed = scans.countByOwnerAndStatus(user, ScanStatus.COMPLETED);
        Instant lastScan = userScans.isEmpty() ? null : userScans.getFirst().getCreatedAt();

        Map<String, Long> technologies = new LinkedHashMap<>();
        userAssets.stream().flatMap(asset -> asset.getTechnologies().stream())
                .forEach(tech -> technologies.merge(tech, 1L, Long::sum));

        Map<String, Long> severities = Map.of(
                "critical", findings.countByOwnerAndSeverity(user, Severity.CRITICAL),
                "high", findings.countByOwnerAndSeverity(user, Severity.HIGH),
                "medium", findings.countByOwnerAndSeverity(user, Severity.MEDIUM),
                "low", findings.countByOwnerAndSeverity(user, Severity.LOW),
                "informational", findings.countByOwnerAndSeverity(user, Severity.INFORMATIONAL)
        );

        List<Map<String, Object>> riskTrend = userScans.stream()
                .limit(12)
                .map(scan -> Map.<String, Object>of("date", scan.getCreatedAt(), "risk", scan.getRiskScore()))
                .toList();

        return new DashboardDtos.Stats(
                userScans.stream().map(Scan::getDomain).distinct().count(),
                total,
                assets.countByOwner(user),
                assets.countByOwnerAndStatus(user, "alive"),
                severities.get("critical"),
                severities.get("high"),
                severities.get("medium"),
                severities.get("low"),
                lastScan,
                total == 0 ? 0 : (completed * 100.0 / total),
                List.of(Map.of("month", "Jun", "assets", userAssets.size())),
                severities,
                technologies,
                riskTrend
        );
    }
}
