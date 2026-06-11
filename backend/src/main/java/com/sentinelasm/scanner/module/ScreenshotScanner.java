package com.sentinelasm.scanner.module;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(50)
public class ScreenshotScanner implements ScannerModule {
    @Override
    public String name() {
        return "Capturing Screenshots";
    }

    @Override
    public void execute(ScannerContext context) {
        for (ScannerContext.AssetBuilder asset : context.assets()) {
            asset.screenshotPath("/screenshots/" + asset.hostname() + ".png");
        }
        context.log("Screenshot paths queued for live hosts.");
    }
}
