package com.sentinelasm.scanner.module;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sentinelasm.scanner.ScanResult;
import com.sentinelasm.scanner.ScannerProperties;
import com.sentinelasm.scanner.ToolExecutor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

@Service
public class ScanOrchestrator {
    private final List<ScannerModule> modules;
    private final ScannerProperties properties;
    private final ToolExecutor executor;
    private final ObjectMapper objectMapper;

    public ScanOrchestrator(List<ScannerModule> modules, ScannerProperties properties, ToolExecutor executor, ObjectMapper objectMapper) {
        this.modules = modules.stream()
                .sorted(Comparator.comparing(module -> module.getClass().getAnnotation(org.springframework.core.annotation.Order.class).value()))
                .toList();
        this.properties = properties;
        this.executor = executor;
        this.objectMapper = objectMapper;
    }

    public ScanResult execute(String domain, Consumer<String> log) throws Exception {
        ScannerContext context = new ScannerContext(domain, properties, executor, objectMapper, log);
        for (ScannerModule module : modules) {
            context.log(module.name());
            module.execute(context);
        }
        return context.toResult();
    }
}
