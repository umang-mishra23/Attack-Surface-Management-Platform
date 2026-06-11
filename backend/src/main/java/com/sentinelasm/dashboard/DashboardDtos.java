package com.sentinelasm.dashboard;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public class DashboardDtos {
    public record Stats(
            long totalDomains,
            long totalScans,
            long totalAssets,
            long aliveHosts,
            long criticalFindings,
            long highFindings,
            long mediumFindings,
            long lowFindings,
            Instant lastScanTime,
            double successRate,
            List<Map<String, Object>> monthlyTrend,
            Map<String, Long> severityDistribution,
            Map<String, Long> technologyDistribution,
            List<Map<String, Object>> riskTrend
    ) {}
}
