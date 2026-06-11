package com.sentinelasm.scanner.module;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(65)
public class SslScanner implements ScannerModule {
    @Override
    public String name() {
        return "Running SSL Analysis";
    }

    @Override
    public void execute(ScannerContext context) {
        context.log("SSL analysis module registered. Certificate parsing can be added without changing orchestration.");
    }
}
