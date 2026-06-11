package com.sentinelasm.scanner;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sentinelasm.domain.Severity;
import com.sentinelasm.scanner.module.ScanOrchestrator;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ReconEngine {
    private final ScannerProperties properties;
    private final ToolExecutor executor;
    private final ObjectMapper objectMapper;
    private final ScanOrchestrator scanOrchestrator;

    public ReconEngine(ScannerProperties properties, ToolExecutor executor, ObjectMapper objectMapper, ScanOrchestrator scanOrchestrator) {
        this.properties = properties;
        this.executor = executor;
        this.objectMapper = objectMapper;
        this.scanOrchestrator = scanOrchestrator;
    }

    public ScanResult run(String domain, Consumer<String> log) throws Exception {
        log.accept("Scanner mode: " + properties.getMode());
        if (!"tools".equalsIgnoreCase(properties.getMode())) {
            log.accept("Using mock/demo scanner data. Set ASM_SCAN_MODE=tools to execute SubMerge.");
            return mock(domain, log);
        }

        log.accept("Using real tool scanner pipeline.");
        return scanOrchestrator.execute(domain, log);
    }

    private ScanResult runTools(String domain, Consumer<String> log) throws Exception {
        Duration timeout = Duration.ofSeconds(properties.getTimeoutSeconds());
        Path workspace = Files.createDirectories(Path.of(properties.getWorkspace(), sanitize(domain) + "-" + System.currentTimeMillis()));
        ScanResult result = new ScanResult();

        log.accept("Initializing real tool scan workspace: " + workspace);
        List<String> subdomains = runSubMerge(domain, workspace, timeout, log);
        if (subdomains.isEmpty()) {
            log.accept("SubMerge returned no subdomains. Adding root domain for enrichment.");
            subdomains = List.of(domain);
        }
        Files.write(workspace.resolve("subdomains.txt"), subdomains);

        Map<String, HttpxRecord> httpx = runHttpx(workspace, timeout, log);
        List<String> aliveUrls = httpx.values().stream().map(HttpxRecord::url).filter(value -> value != null && !value.isBlank()).distinct().toList();
        Files.write(workspace.resolve("alive-urls.txt"), aliveUrls);

        Map<String, List<ScanResult.PortFinding>> portsByHost = runNmap(httpx, workspace, timeout, log);
        Map<String, List<String>> whatWebTech = runWhatWeb(aliveUrls, workspace, timeout, log);
        List<ScanResult.VulnerabilityFinding> nucleiFindings = runNuclei(aliveUrls, workspace, timeout, log);

        for (String subdomain : subdomains) {
            HttpxRecord http = httpx.get(subdomain);
            String ip = firstNonBlank(http == null ? null : http.ip(), resolveIp(subdomain), "unresolved");
            String url = http == null ? "https://" + subdomain : http.url();
            Set<String> technologies = new LinkedHashSet<>();
            if (http != null) technologies.addAll(http.technologies());
            technologies.addAll(whatWebTech.getOrDefault(url, List.of()));

            List<ScanResult.PortFinding> ports = portsByHost.getOrDefault(subdomain, inferPorts(url));
            result.assets.add(new ScanResult.AssetFinding(
                    subdomain,
                    ip,
                    url,
                    http == null ? "discovered" : "alive",
                    http == null ? null : http.statusCode(),
                    http == null ? "Discovered by SubMerge" : http.title(),
                    new ArrayList<>(technologies),
                    ports,
                    "/screenshots/" + subdomain + ".png"
            ));
        }

        result.findings.addAll(nucleiFindings);
        log.accept("Normalizing tool output...");
        return result;
    }

    private List<String> runSubMerge(String domain, Path workspace, Duration timeout, Consumer<String> log) throws Exception {
        log.accept("Running SubMerge...");
        List<String> lines = executor.run(List.of("bash", properties.getTools().getSubmerge(), domain), workspace, timeout, log);
        Pattern hostPattern = Pattern.compile("(?i)(?:[a-z0-9-]+\\.)+" + Pattern.quote(domain));
        return lines.stream()
                .flatMap(line -> {
                    List<String> hosts = new ArrayList<>();
                    Matcher matcher = hostPattern.matcher(line);
                    while (matcher.find()) hosts.add(matcher.group().toLowerCase(Locale.ROOT));
                    return hosts.stream();
                })
                .distinct()
                .toList();
    }

    private Map<String, HttpxRecord> runHttpx(Path workspace, Duration timeout, Consumer<String> log) throws Exception {
        log.accept("Running HTTPX...");
        Path input = workspace.resolve("subdomains.txt");
        Path output = workspace.resolve("httpx.jsonl");
        executor.run(List.of(properties.getTools().getHttpx(), "-json", "-title", "-status-code", "-tech-detect", "-ip", "-l", input.toString(), "-o", output.toString()),
                workspace, timeout, log);

        Map<String, HttpxRecord> records = new LinkedHashMap<>();
        if (!Files.exists(output)) return records;
        for (String line : Files.readAllLines(output)) {
            if (line.isBlank()) continue;
            Map<String, Object> json = objectMapper.readValue(line, new TypeReference<>() {});
            String inputHost = string(json.get("input"));
            String url = string(json.get("url"));
            String host = hostFromUrl(url).orElse(inputHost);
            Integer statusCode = integer(json.get("status_code"));
            String title = firstNonBlank(string(json.get("title")), "Live Host");
            String ip = firstListValue(json.get("a"));
            if (ip == null) ip = string(json.get("host"));
            List<String> technologies = stringList(json.get("tech"));
            records.put(host, new HttpxRecord(host, url, ip, statusCode, title, technologies));
            log.accept("HTTPX alive: " + host + " " + statusCode);
        }
        return records;
    }

    private Map<String, List<ScanResult.PortFinding>> runNmap(Map<String, HttpxRecord> httpx, Path workspace, Duration timeout, Consumer<String> log) throws Exception {
        Map<String, List<ScanResult.PortFinding>> ports = new LinkedHashMap<>();
        if (httpx.isEmpty()) return ports;

        for (String host : httpx.keySet()) {
            log.accept("Running Nmap for " + host + "...");
            Path xml = workspace.resolve("nmap-" + sanitize(host) + ".xml");
            executor.run(List.of(properties.getTools().getNmap(), "-sV", "--open", "-oX", xml.toString(), host), workspace, timeout, log);
            ports.put(host, parseNmapXml(xml));
        }
        return ports;
    }

    private List<ScanResult.PortFinding> parseNmapXml(Path xml) throws Exception {
        List<ScanResult.PortFinding> ports = new ArrayList<>();
        if (!Files.exists(xml)) return ports;
        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(xml.toFile());
        NodeList nodes = document.getElementsByTagName("port");
        for (int i = 0; i < nodes.getLength(); i++) {
            Element port = (Element) nodes.item(i);
            Element state = (Element) port.getElementsByTagName("state").item(0);
            Element service = (Element) port.getElementsByTagName("service").item(0);
            ports.add(new ScanResult.PortFinding(
                    Integer.parseInt(port.getAttribute("portid")),
                    port.getAttribute("protocol"),
                    service == null ? "" : service.getAttribute("name"),
                    service == null ? "" : firstNonBlank(service.getAttribute("product"), service.getAttribute("version")),
                    state == null ? "unknown" : state.getAttribute("state")
            ));
        }
        return ports;
    }

    private Map<String, List<String>> runWhatWeb(List<String> urls, Path workspace, Duration timeout, Consumer<String> log) throws Exception {
        Map<String, List<String>> technologies = new LinkedHashMap<>();
        for (String url : urls) {
            log.accept("Running WhatWeb for " + url + "...");
            Path output = workspace.resolve("whatweb-" + sanitize(url) + ".json");
            executor.run(List.of(properties.getTools().getWhatweb(), "--log-json=" + output, url), workspace, timeout, log);
            technologies.put(url, parseWhatWeb(output));
        }
        return technologies;
    }

    private List<String> parseWhatWeb(Path output) throws Exception {
        if (!Files.exists(output) || Files.size(output) == 0) return List.of();
        Object parsed = objectMapper.readValue(output.toFile(), Object.class);
        List<String> tech = new ArrayList<>();
        if (parsed instanceof List<?> list) {
            for (Object item : list) collectPluginNames(item, tech);
        } else {
            collectPluginNames(parsed, tech);
        }
        return tech.stream().distinct().toList();
    }

    @SuppressWarnings("unchecked")
    private void collectPluginNames(Object item, List<String> tech) {
        if (!(item instanceof Map<?, ?> map)) return;
        Object plugins = map.get("plugins");
        if (plugins instanceof Map<?, ?> pluginMap) {
            pluginMap.keySet().forEach(key -> tech.add(String.valueOf(key)));
        } else if (map.containsKey("name")) {
            tech.add(String.valueOf(map.get("name")));
        }
    }

    private List<ScanResult.VulnerabilityFinding> runNuclei(List<String> urls, Path workspace, Duration timeout, Consumer<String> log) throws Exception {
        if (urls.isEmpty()) return List.of();
        log.accept("Running Nuclei...");
        Path input = workspace.resolve("alive-urls.txt");
        Path output = workspace.resolve("nuclei.jsonl");
        executor.run(List.of(properties.getTools().getNuclei(), "-jsonl", "-l", input.toString(), "-o", output.toString()), workspace, timeout, log);
        if (!Files.exists(output)) return List.of();

        List<ScanResult.VulnerabilityFinding> findings = new ArrayList<>();
        for (String line : Files.readAllLines(output)) {
            if (line.isBlank()) continue;
            Map<String, Object> json = objectMapper.readValue(line, new TypeReference<>() {});
            Map<String, Object> info = map(json.get("info"));
            String matched = firstNonBlank(string(json.get("matched-at")), string(json.get("host")), string(json.get("url")));
            Severity severity = severity(string(info.get("severity")));
            findings.add(new ScanResult.VulnerabilityFinding(
                    hostFromUrl(matched).orElse(matched),
                    severity,
                    firstNonBlank(string(info.get("name")), string(json.get("template-id")), "Nuclei finding"),
                    firstNonBlank(string(info.get("description")), "Nuclei detected a security-relevant condition."),
                    matched,
                    firstNonBlank(string(json.get("template-id")), string(json.get("template"))),
                    firstListValue(info.get("classification") instanceof Map<?, ?> c ? c.get("cve-id") : null),
                    doubleValue(info.get("classification") instanceof Map<?, ?> c ? c.get("cvss-score") : null),
                    firstNonBlank(firstListValue(info.get("remediation")), "Review the affected service and follow vendor or template guidance.")
            ));
            log.accept("Nuclei finding: " + findings.getLast().name() + " [" + severity + "]");
        }
        return findings;
    }

    private ScanResult mock(String domain, Consumer<String> log) {
        List<String> messages = List.of(
                "Initializing Scan...",
                "Running SubMerge...",
                "Found api." + domain,
                "Found vpn." + domain,
                "Running HTTPX...",
                "Checking Alive Hosts...",
                "Running Nmap...",
                "Running WhatWeb...",
                "Running Nuclei...",
                "Saving Results...",
                "Generating Report...",
                "Completed."
        );
        messages.forEach(log);

        ScanResult result = new ScanResult();
        result.assets.add(new ScanResult.AssetFinding(domain, "203.0.113.10", "https://" + domain, "alive", 200, "Corporate Site",
                List.of("Nginx", "React", "HTTP/2"), List.of(port(80, "http"), port(443, "https")), "/screenshots/" + domain + ".png"));
        result.assets.add(new ScanResult.AssetFinding("api." + domain, "203.0.113.20", "https://api." + domain, "alive", 200, "Public API",
                List.of("Spring Boot", "OpenAPI", "PostgreSQL"), List.of(port(443, "https"), port(8080, "http-proxy")), "/screenshots/api." + domain + ".png"));
        result.assets.add(new ScanResult.AssetFinding("admin." + domain, "203.0.113.30", "https://admin." + domain, "alive", 200, "Admin Console",
                List.of("React", "NodeJS", "Nginx"), List.of(port(443, "https")), "/screenshots/admin." + domain + ".png"));
        result.assets.add(new ScanResult.AssetFinding("vpn." + domain, "203.0.113.40", "vpn." + domain, "alive", null, "VPN Gateway",
                List.of("OpenVPN", "TLS"), List.of(port(443, "https"), port(1194, "openvpn")), null));

        result.findings.add(new ScanResult.VulnerabilityFinding("admin." + domain, Severity.HIGH,
                "Internet-facing administrative console",
                "Administrative interface is reachable from the public internet.",
                "https://admin." + domain,
                "exposure/admin-panel",
                null,
                7.2,
                "Restrict access behind VPN, identity-aware proxy, or source allowlisting."));
        result.findings.add(new ScanResult.VulnerabilityFinding("api." + domain, Severity.MEDIUM,
                "Missing HSTS header",
                "HTTPS service does not advertise Strict-Transport-Security.",
                "https://api." + domain,
                "http/missing-hsts",
                null,
                4.3,
                "Add Strict-Transport-Security after validating HTTPS coverage."));
        result.findings.add(new ScanResult.VulnerabilityFinding("vpn." + domain, Severity.LOW,
                "Externally exposed VPN endpoint",
                "VPN service is internet-facing and should be tracked as a perimeter asset.",
                "vpn." + domain.toLowerCase(Locale.ROOT),
                "network/vpn-exposure",
                null,
                2.1,
                "Enforce MFA, keep the appliance patched, and monitor authentication telemetry."));
        return result;
    }

    private ScanResult.PortFinding port(int port, String service) {
        return new ScanResult.PortFinding(port, "tcp", service, "", "open");
    }

    private List<ScanResult.PortFinding> inferPorts(String url) {
        if (url == null) return List.of();
        if (url.startsWith("https://")) return List.of(port(443, "https"));
        if (url.startsWith("http://")) return List.of(port(80, "http"));
        return List.of();
    }

    private String resolveIp(String host) {
        try {
            return InetAddress.getByName(host).getHostAddress();
        } catch (Exception ignored) {
            return null;
        }
    }

    private Optional<String> hostFromUrl(String value) {
        if (value == null || value.isBlank()) return Optional.empty();
        String normalized = value.replaceFirst("^[a-zA-Z][a-zA-Z0-9+.-]*://", "");
        int slash = normalized.indexOf('/');
        if (slash >= 0) normalized = normalized.substring(0, slash);
        int colon = normalized.indexOf(':');
        if (colon >= 0) normalized = normalized.substring(0, colon);
        return normalized.isBlank() ? Optional.empty() : Optional.of(normalized.toLowerCase(Locale.ROOT));
    }

    private String sanitize(String value) {
        return value.replaceAll("[^a-zA-Z0-9.-]", "_");
    }

    private String string(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) return value;
        }
        return null;
    }

    private Integer integer(Object value) {
        if (value instanceof Number number) return number.intValue();
        try {
            return value == null ? null : Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private Double doubleValue(Object value) {
        if (value instanceof Number number) return number.doubleValue();
        try {
            return value == null ? null : Double.parseDouble(String.valueOf(value));
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private List<String> stringList(Object value) {
        if (value instanceof List<?> list) {
            return list.stream().map(String::valueOf).filter(item -> !item.isBlank()).toList();
        }
        if (value instanceof String text && !text.isBlank()) return List.of(text);
        return List.of();
    }

    private String firstListValue(Object value) {
        if (value instanceof List<?> list && !list.isEmpty()) return string(list.getFirst());
        return string(value);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> map(Object value) {
        return value instanceof Map<?, ?> map ? (Map<String, Object>) map : Map.of();
    }

    private Severity severity(String value) {
        if (value == null) return Severity.INFORMATIONAL;
        return switch (value.toLowerCase(Locale.ROOT)) {
            case "critical" -> Severity.CRITICAL;
            case "high" -> Severity.HIGH;
            case "medium" -> Severity.MEDIUM;
            case "low" -> Severity.LOW;
            default -> Severity.INFORMATIONAL;
        };
    }

    private record HttpxRecord(String host, String url, String ip, Integer statusCode, String title, List<String> technologies) {}
}
