package com.sentinelasm.scanner.module;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(60)
public class WhoisScanner implements ScannerModule {
    @Override
    public String name() {
        return "Running WHOIS Lookup";
    }

    @Override
    public void execute(ScannerContext context) {
        context.log("WHOIS module registered. Add provider-backed WHOIS enrichment here.");
    }
}
