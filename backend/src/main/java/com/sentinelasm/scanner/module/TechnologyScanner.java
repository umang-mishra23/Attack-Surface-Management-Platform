package com.sentinelasm.scanner.module;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

@Component
@Order(30)
public class TechnologyScanner implements ScannerModule {
    @Override
    public String name() {
        return "Running Technology Detection";
    }

    @Override
    public void execute(ScannerContext context) throws Exception {
        Path workspace = Files.createDirectories(Path.of(context.properties().getWorkspace(), "whatweb-" + System.currentTimeMillis()));
        for (String url : context.aliveUrls()) {
            String host = ScannerContext.hostFromUrl(url);
            Path output = workspace.resolve(host.replaceAll("[^a-zA-Z0-9.-]", "_") + ".json");
            context.executor().run(
                    List.of(context.properties().getTools().getWhatweb(), "--log-json=" + output, url),
                    workspace,
                    context.timeout(),
                    line -> context.log("[WhatWeb] " + line)
            );
            for (String technology : parse(context, output)) {
                context.asset(host).addTechnology(technology);
            }
        }
    }

    private List<String> parse(ScannerContext context, Path output) throws Exception {
        if (!Files.exists(output) || Files.size(output) == 0) return List.of();
        Object parsed = context.objectMapper().readValue(output.toFile(), Object.class);
        java.util.Set<String> technologies = new java.util.LinkedHashSet<>();
        if (parsed instanceof List<?> list) list.forEach(item -> collect(item, technologies));
        else collect(parsed, technologies);
        return technologies.stream().toList();
    }

    private void collect(Object item, java.util.Set<String> technologies) {
        if (!(item instanceof Map<?, ?> map)) return;
        Object plugins = map.get("plugins");
        if (plugins instanceof Map<?, ?> pluginMap) {
            pluginMap.keySet().forEach(key -> technologies.add(String.valueOf(key)));
        }
    }
}
