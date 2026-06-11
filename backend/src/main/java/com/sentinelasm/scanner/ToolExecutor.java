package com.sentinelasm.scanner;

import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

@Component
public class ToolExecutor {
    public List<String> run(List<String> command, Duration timeout, Consumer<String> log) throws Exception {
        return run(command, null, timeout, log);
    }

    public List<String> run(List<String> command, Path workingDirectory, Duration timeout, Consumer<String> log) throws Exception {
        ProcessBuilder builder = new ProcessBuilder(command);
        if (workingDirectory != null) {
            builder.directory(workingDirectory.toFile());
        }
        builder.redirectErrorStream(true);
        Process process = builder.start();
        List<String> lines = new ArrayList<>();

        try (var readerPool = Executors.newSingleThreadExecutor()) {
            Future<?> reader = readerPool.submit(() -> {
                try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        synchronized (lines) {
                            lines.add(line);
                        }
                        log.accept(line);
                    }
                } catch (Exception error) {
                    log.accept("Tool output reader failed: " + error.getMessage());
                }
            });

            boolean exited = process.waitFor(timeout.toSeconds(), TimeUnit.SECONDS);
            if (!exited) {
                process.destroyForcibly();
                reader.cancel(true);
                throw new IllegalStateException("Tool timed out: " + String.join(" ", command));
            }

            try {
                reader.get(5, TimeUnit.SECONDS);
            } catch (TimeoutException error) {
                log.accept("Tool output reader did not finish cleanly within 5 seconds after process exit.");
            }
        }

        if (process.exitValue() != 0) {
            throw new IllegalStateException("Tool failed: " + String.join(" ", command));
        }
        return lines;
    }
}
