package com.sentinelasm.system;

import com.sentinelasm.scanner.ScannerProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Files;
import java.nio.file.Path;

@RestController
@RequestMapping("/api/system")
public class SystemStatusController {
    private final ScannerProperties scannerProperties;

    public SystemStatusController(ScannerProperties scannerProperties) {
        this.scannerProperties = scannerProperties;
    }

    @GetMapping("/scanner")
    public ScannerStatus scanner() {
        Path projectRoot = Path.of(scannerProperties.getProjectRoot()).toAbsolutePath().normalize();
        Path submerge = projectRoot.resolve(scannerProperties.getTools().getSubmerge()).normalize();
        boolean toolsMode = "tools".equalsIgnoreCase(scannerProperties.getMode());
        return new ScannerStatus(
                scannerProperties.getMode(),
                toolsMode,
                projectRoot.toString(),
                submerge.toString(),
                Files.exists(submerge),
                scannerProperties.getOutputRoot()
        );
    }

    public record ScannerStatus(
            String mode,
            boolean realToolsEnabled,
            String projectRoot,
            String submergePath,
            boolean submergeExists,
            String outputRoot
    ) {}
}
