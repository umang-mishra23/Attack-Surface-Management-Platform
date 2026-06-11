package com.sentinelasm.scanner.module;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(75)
public class ApiDiscoveryScanner implements ScannerModule {
    @Override
    public String name() {
        return "Running API Discovery";
    }

    @Override
    public void execute(ScannerContext context) {
        context.log("API discovery module registered. OpenAPI and route discovery can plug in here.");
    }
}
