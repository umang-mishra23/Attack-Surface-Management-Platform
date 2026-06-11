package com.sentinelasm.scan;

import com.sentinelasm.domain.ScanStatus;
import com.sentinelasm.user.AppUser;
import com.sentinelasm.user.UserService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.Executors;

@RestController
@RequestMapping("/api/scans")
public class ScanEventController {
    private final ScanRepository scans;
    private final UserService userService;

    public ScanEventController(ScanRepository scans, UserService userService) {
        this.scans = scans;
        this.userService = userService;
    }

    @GetMapping(value = "/{id}/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter events(@PathVariable UUID id) {
        AppUser owner = userService.currentUser();
        SseEmitter emitter = new SseEmitter(0L);

        Executors.newSingleThreadExecutor().submit(() -> {
            int sentLogs = 0;
            try {
                while (true) {
                    Scan scan = scans.findByIdAndOwner(id, owner)
                            .orElseThrow(() -> new IllegalArgumentException("Scan not found"));

                    emitter.send(SseEmitter.event().name("status").data(new ScanEvent(
                            scan.getStatus().name(),
                            scan.getProgress(),
                            scan.getRiskScore(),
                            scan.getLogs().size() > sentLogs ? scan.getLogs().subList(sentLogs, scan.getLogs().size()) : java.util.List.of()
                    )));
                    sentLogs = scan.getLogs().size();

                    if (scan.getStatus() == ScanStatus.COMPLETED || scan.getStatus() == ScanStatus.FAILED || scan.getStatus() == ScanStatus.CANCELLED) {
                        emitter.complete();
                        return;
                    }
                    Thread.sleep(1500);
                }
            } catch (IOException error) {
                emitter.completeWithError(error);
            } catch (Exception error) {
                emitter.completeWithError(error);
            }
        });

        return emitter;
    }

    public record ScanEvent(String status, int progress, int riskScore, java.util.List<String> logs) {}
}
