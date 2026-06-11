package com.sentinelasm.scanner.module;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Order(10)
public class SubMergeScanner implements ScannerModule {
    private static final Pattern ALIVE_LINE = Pattern.compile("^(https?://\\S+)(?:\\s+\\[(\\d{3})])?.*$");

    @Override
    public String name() {
        return "Executing SubMerge";
    }

    @Override
    public void execute(ScannerContext context) throws Exception {
        Path script = context.projectRoot().resolve(context.properties().getTools().getSubmerge()).normalize();
        Path outputBase = context.projectRoot()
                .resolve(context.properties().getOutputRoot())
                .resolve(context.domain())
                .normalize();
        String scriptArgument = context.properties().getTools().getSubmerge().replace("\\", "/");

        context.log("SubMerge script: " + script);
        context.log("SubMerge output: " + outputBase);
        if (!Files.exists(script)) {
            throw new IllegalStateException("SubMerge script not found at " + script);
        }
        context.executor().run(
                List.of("bash", scriptArgument, context.domain()),
                context.projectRoot(),
                context.timeout(),
                line -> context.log("[SubMerge] " + line)
        );

        Path runLog = outputBase.resolve("run.log");
        if (Files.exists(runLog)) {
            context.log("Importing SubMerge run.log");
            for (String line : Files.readAllLines(runLog)) {
                if (!line.isBlank()) context.log("[SubMerge Log] " + line);
            }
        }

        importResolved(context, outputBase.resolve("final").resolve("resolved.txt"));
        importAlive(context, outputBase.resolve("final").resolve("alive.txt"));
    }

    private void importResolved(ScannerContext context, Path resolvedFile) throws Exception {
        context.log("Importing resolved assets");
        if (!Files.exists(resolvedFile)) {
            context.log("resolved.txt not found: " + resolvedFile);
            return;
        }
        for (String line : Files.readAllLines(resolvedFile)) {
            String host = line.trim();
            if (host.isBlank()) continue;
            context.asset(host).status("resolved").pageTitle("DNS Resolved Asset");
        }
    }

    private void importAlive(ScannerContext context, Path aliveFile) throws Exception {
        context.log("Importing alive hosts");
        if (!Files.exists(aliveFile)) {
            context.log("alive.txt not found: " + aliveFile);
            return;
        }
        for (String line : Files.readAllLines(aliveFile)) {
            Matcher matcher = ALIVE_LINE.matcher(line.trim());
            if (!matcher.matches()) continue;
            String url = matcher.group(1);
            String host = ScannerContext.hostFromUrl(url);
            Integer status = matcher.group(2) == null ? null : Integer.parseInt(matcher.group(2));
            context.asset(host)
                    .url(url)
                    .status("alive")
                    .responseCode(status)
                    .pageTitle("Live Host");
            context.addAliveUrl(url);
        }
    }
}
