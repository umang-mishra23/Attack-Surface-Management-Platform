package com.sentinelasm.scanner.module;

public interface ScannerModule {
    String name();
    void execute(ScannerContext context) throws Exception;
}
