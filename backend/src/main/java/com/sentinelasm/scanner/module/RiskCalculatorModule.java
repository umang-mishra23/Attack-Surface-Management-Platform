package com.sentinelasm.scanner.module;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(90)
public class RiskCalculatorModule implements ScannerModule {
    @Override
    public String name() {
        return "Calculating Risk Score";
    }

    @Override
    public void execute(ScannerContext context) {
        context.log("Risk score will be calculated from normalized findings during persistence.");
    }
}
