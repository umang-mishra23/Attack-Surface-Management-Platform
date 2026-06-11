package com.sentinelasm.scanner.module;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sentinelasm.domain.Severity;
import com.sentinelasm.scanner.ScanResult;
import com.sentinelasm.scanner.ScannerProperties;
import com.sentinelasm.scanner.ToolExecutor;

import java.net.URI;
import java.nio.file.Path;
import java.time.Duration;
import java.util.*;
import java.util.function.Consumer;

public class ScannerContext {
    private final String domain;
    private final ScannerProperties properties;
    private final ToolExecutor executor;
    private final ObjectMapper objectMapper;
    private final Consumer<String> log;
    private final Path projectRoot;
    private final Duration timeout;
    private final Map<String, AssetBuilder> assets = new LinkedHashMap<>();
    private final List<ScanResult.VulnerabilityFinding> findings = new ArrayList<>();
    private final List<String> aliveUrls = new ArrayList<>();

    public ScannerContext(String domain, ScannerProperties properties, ToolExecutor executor, ObjectMapper objectMapper, Consumer<String> log) {
        this.domain = domain;
        this.properties = properties;
        this.executor = executor;
        this.objectMapper = objectMapper;
        this.log = log;
        this.projectRoot = Path.of(properties.getProjectRoot()).toAbsolutePath().normalize();
        this.timeout = Duration.ofSeconds(properties.getTimeoutSeconds());
    }

    public String domain() { return domain; }
    public ScannerProperties properties() { return properties; }
    public ToolExecutor executor() { return executor; }
    public ObjectMapper objectMapper() { return objectMapper; }
    public Path projectRoot() { return projectRoot; }
    public Duration timeout() { return timeout; }
    public List<String> aliveUrls() { return aliveUrls; }
    public List<ScanResult.VulnerabilityFinding> findings() { return findings; }
    public Collection<AssetBuilder> assets() { return assets.values(); }

    public void log(String message) {
        log.accept(message);
    }

    public AssetBuilder asset(String hostname) {
        String normalized = normalizeHost(hostname);
        return assets.computeIfAbsent(normalized, AssetBuilder::new);
    }

    public void addAliveUrl(String url) {
        if (url != null && !url.isBlank() && !aliveUrls.contains(url)) {
            aliveUrls.add(url);
        }
    }

    public ScanResult toResult() {
        ScanResult result = new ScanResult();
        for (AssetBuilder asset : assets.values()) {
            result.assets.add(asset.toFinding());
        }
        result.findings.addAll(findings);
        return result;
    }

    public static String hostFromUrl(String value) {
        if (value == null || value.isBlank()) return "";
        try {
            String withScheme = value.matches("^[a-zA-Z][a-zA-Z0-9+.-]*://.*") ? value : "https://" + value;
            return URI.create(withScheme).getHost();
        } catch (Exception ignored) {
            String normalized = value.replaceFirst("^[a-zA-Z][a-zA-Z0-9+.-]*://", "");
            int slash = normalized.indexOf('/');
            if (slash >= 0) normalized = normalized.substring(0, slash);
            int colon = normalized.indexOf(':');
            if (colon >= 0) normalized = normalized.substring(0, colon);
            return normalized;
        }
    }

    public static String normalizeHost(String host) {
        return host == null ? "" : host.toLowerCase(Locale.ROOT).trim();
    }

    public static Severity severity(String value) {
        if (value == null) return Severity.INFORMATIONAL;
        return switch (value.toLowerCase(Locale.ROOT)) {
            case "critical" -> Severity.CRITICAL;
            case "high" -> Severity.HIGH;
            case "medium" -> Severity.MEDIUM;
            case "low" -> Severity.LOW;
            default -> Severity.INFORMATIONAL;
        };
    }

    public static class AssetBuilder {
        private final String hostname;
        private String ipAddress = "unresolved";
        private String url;
        private String status = "discovered";
        private Integer responseCode;
        private String pageTitle = "Discovered Asset";
        private final Set<String> technologies = new LinkedHashSet<>();
        private final List<ScanResult.PortFinding> ports = new ArrayList<>();
        private String screenshotPath;

        AssetBuilder(String hostname) {
            this.hostname = hostname;
        }

        public String hostname() { return hostname; }
        public String url() { return url; }
        public AssetBuilder ipAddress(String ipAddress) { if (ipAddress != null && !ipAddress.isBlank()) this.ipAddress = ipAddress; return this; }
        public AssetBuilder url(String url) { if (url != null && !url.isBlank()) this.url = url; return this; }
        public AssetBuilder status(String status) { if (status != null && !status.isBlank()) this.status = status; return this; }
        public AssetBuilder responseCode(Integer responseCode) { if (responseCode != null) this.responseCode = responseCode; return this; }
        public AssetBuilder pageTitle(String pageTitle) { if (pageTitle != null && !pageTitle.isBlank()) this.pageTitle = pageTitle; return this; }
        public AssetBuilder addTechnology(String technology) { if (technology != null && !technology.isBlank()) technologies.add(technology); return this; }
        public AssetBuilder addTechnologies(Collection<String> values) { if (values != null) values.forEach(this::addTechnology); return this; }
        public AssetBuilder addPort(ScanResult.PortFinding port) { if (port != null) ports.add(port); return this; }
        public AssetBuilder screenshotPath(String screenshotPath) { this.screenshotPath = screenshotPath; return this; }

        ScanResult.AssetFinding toFinding() {
            return new ScanResult.AssetFinding(
                    hostname,
                    ipAddress,
                    url == null ? "https://" + hostname : url,
                    status,
                    responseCode,
                    pageTitle,
                    new ArrayList<>(technologies),
                    ports,
                    screenshotPath
            );
        }
    }
}
