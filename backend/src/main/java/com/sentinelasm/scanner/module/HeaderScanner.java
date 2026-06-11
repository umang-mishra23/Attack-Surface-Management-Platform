package com.sentinelasm.scanner.module;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(40)
public class HeaderScanner implements ScannerModule {
    @Override
    public String name() {
        return "Running Security Header Analysis";
    }

    @Override
    public void execute(ScannerContext context) {
        context.log("Header analysis module registered. Full header persistence is the next enrichment step.");
    }
}
