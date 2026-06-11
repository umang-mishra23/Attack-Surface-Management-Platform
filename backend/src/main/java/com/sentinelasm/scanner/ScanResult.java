package com.sentinelasm.scanner;

import com.sentinelasm.domain.Severity;

import java.util.ArrayList;
import java.util.List;

public class ScanResult {
    public List<AssetFinding> assets = new ArrayList<>();
    public List<VulnerabilityFinding> findings = new ArrayList<>();

    public record AssetFinding(
            String subdomain,
            String ipAddress,
            String url,
            String status,
            Integer responseCode,
            String pageTitle,
            List<String> technologies,
            List<PortFinding> ports,
            String screenshotPath
    ) {}

    public record PortFinding(
            int port,
            String protocol,
            String service,
            String version,
            String state
    ) {}

    public record VulnerabilityFinding(
            String asset,
            Severity severity,
            String name,
            String description,
            String affectedUrl,
            String template,
            String cve,
            Double cvss,
            String recommendation
    ) {}
}
