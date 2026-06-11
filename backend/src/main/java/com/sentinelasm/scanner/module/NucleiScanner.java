package com.sentinelasm.scanner.module;

import com.fasterxml.jackson.core.type.TypeReference;
import com.sentinelasm.scanner.ScanResult;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

@Component
@Order(80)
public class NucleiScanner implements ScannerModule {
    @Override
    public String name() {
        return "Running Nuclei";
    }

    @Override
    public void execute(ScannerContext context) throws Exception {
        if (context.aliveUrls().isEmpty()) {
            context.log("No alive URLs for Nuclei");
            return;
        }
        Path workspace = Files.createDirectories(Path.of(context.properties().getWorkspace(), "nuclei-" + System.currentTimeMillis()));
        Path input = workspace.resolve("alive-urls.txt");
        Path output = workspace.resolve("nuclei.jsonl");
        Files.write(input, context.aliveUrls());

        context.executor().run(
                List.of(context.properties().getTools().getNuclei(), "-jsonl", "-l", input.toString(), "-o", output.toString()),
                workspace,
                context.timeout(),
                line -> context.log("[Nuclei] " + line)
        );

        if (!Files.exists(output)) return;
        for (String line : Files.readAllLines(output)) {
            if (line.isBlank()) continue;
            Map<String, Object> json = context.objectMapper().readValue(line, new TypeReference<>() {});
            Map<String, Object> info = map(json.get("info"));
            String matched = first(string(json.get("matched-at")), string(json.get("host")), string(json.get("url")));
            String name = first(string(info.get("name")), string(json.get("template-id")), "Nuclei finding");
            context.findings().add(new ScanResult.VulnerabilityFinding(
                    ScannerContext.hostFromUrl(matched),
                    ScannerContext.severity(string(info.get("severity"))),
                    name,
                    first(string(info.get("description")), "Nuclei detected a security-relevant condition."),
                    matched,
                    first(string(json.get("template-id")), string(json.get("template"))),
                    firstListValue(map(info.get("classification")).get("cve-id")),
                    doubleValue(map(info.get("classification")).get("cvss-score")),
                    first(firstListValue(info.get("remediation")), "Review the affected service and follow template guidance.")
            ));
            context.log("Nuclei finding: " + name);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> map(Object value) {
        return value instanceof Map<?, ?> map ? (Map<String, Object>) map : Map.of();
    }

    private String string(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private String first(String... values) {
        for (String value : values) if (value != null && !value.isBlank()) return value;
        return null;
    }

    private String firstListValue(Object value) {
        if (value instanceof List<?> list && !list.isEmpty()) return string(list.getFirst());
        return string(value);
    }

    private Double doubleValue(Object value) {
        if (value instanceof Number number) return number.doubleValue();
        try {
            return value == null ? null : Double.parseDouble(String.valueOf(value));
        } catch (NumberFormatException ignored) {
            return null;
        }
    }
}
