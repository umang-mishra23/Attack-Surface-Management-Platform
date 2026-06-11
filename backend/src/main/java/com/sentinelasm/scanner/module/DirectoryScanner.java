package com.sentinelasm.scanner.module;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(70)
public class DirectoryScanner implements ScannerModule {
    @Override
    public String name() {
        return "Running Directory Enumeration";
    }

    @Override
    public void execute(ScannerContext context) {
        context.log("Directory enumeration module registered. Configure a wordlist scanner here for authorized targets.");
    }
}
