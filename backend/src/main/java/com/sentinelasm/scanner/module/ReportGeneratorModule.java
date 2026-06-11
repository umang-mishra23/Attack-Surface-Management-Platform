package com.sentinelasm.scanner.module;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(100)
public class ReportGeneratorModule implements ScannerModule {
    @Override
    public String name() {
        return "Generating Executive Report";
    }

    @Override
    public void execute(ScannerContext context) {
        context.log("Executive report generation stage reached.");
    }
}
