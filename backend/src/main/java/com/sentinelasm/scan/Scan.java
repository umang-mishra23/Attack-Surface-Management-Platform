package com.sentinelasm.scan;

import com.sentinelasm.domain.Priority;
import com.sentinelasm.domain.ScanStatus;
import com.sentinelasm.domain.ScanType;
import com.sentinelasm.user.AppUser;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "scans")
public class Scan {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private AppUser owner;

    @Column(nullable = false)
    private String domain;

    @Enumerated(EnumType.STRING)
    private ScanType scanType = ScanType.STANDARD;

    @Enumerated(EnumType.STRING)
    private Priority priority = Priority.NORMAL;

    @Enumerated(EnumType.STRING)
    private ScanStatus status = ScanStatus.QUEUED;

    private int progress;
    private int riskScore;
    private String failureReason;
    private Instant createdAt = Instant.now();
    private Instant startedAt;
    private Instant completedAt;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "scan_logs", joinColumns = @JoinColumn(name = "scan_id"))
    @OrderColumn(name = "line_number")
    private List<String> logs = new ArrayList<>();

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public AppUser getOwner() { return owner; }
    public void setOwner(AppUser owner) { this.owner = owner; }
    public String getDomain() { return domain; }
    public void setDomain(String domain) { this.domain = domain; }
    public ScanType getScanType() { return scanType; }
    public void setScanType(ScanType scanType) { this.scanType = scanType; }
    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }
    public ScanStatus getStatus() { return status; }
    public void setStatus(ScanStatus status) { this.status = status; }
    public int getProgress() { return progress; }
    public void setProgress(int progress) { this.progress = progress; }
    public int getRiskScore() { return riskScore; }
    public void setRiskScore(int riskScore) { this.riskScore = riskScore; }
    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getStartedAt() { return startedAt; }
    public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }
    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }
    public List<String> getLogs() { return logs; }
    public void setLogs(List<String> logs) { this.logs = logs; }
}
