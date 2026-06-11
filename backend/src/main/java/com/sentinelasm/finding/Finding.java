package com.sentinelasm.finding;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sentinelasm.asset.Asset;
import com.sentinelasm.domain.FindingStatus;
import com.sentinelasm.domain.Severity;
import com.sentinelasm.scan.Scan;
import com.sentinelasm.user.AppUser;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "findings")
public class Finding {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JsonIgnore
    private AppUser owner;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JsonIgnore
    private Scan scan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    private Asset asset;

    @Enumerated(EnumType.STRING)
    private Severity severity;

    @Enumerated(EnumType.STRING)
    private FindingStatus status = FindingStatus.OPEN;

    private String name;
    @Column(length = 4000)
    private String description;
    private String affectedUrl;
    private String matchedTemplate;
    private String referenceUrl;
    private String cve;
    private Double cvss;
    @Column(length = 4000)
    private String recommendation;
    private Instant dateFound = Instant.now();

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public AppUser getOwner() { return owner; }
    public void setOwner(AppUser owner) { this.owner = owner; }
    public Scan getScan() { return scan; }
    public void setScan(Scan scan) { this.scan = scan; }
    public Asset getAsset() { return asset; }
    public void setAsset(Asset asset) { this.asset = asset; }
    public Severity getSeverity() { return severity; }
    public void setSeverity(Severity severity) { this.severity = severity; }
    public FindingStatus getStatus() { return status; }
    public void setStatus(FindingStatus status) { this.status = status; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getAffectedUrl() { return affectedUrl; }
    public void setAffectedUrl(String affectedUrl) { this.affectedUrl = affectedUrl; }
    public String getMatchedTemplate() { return matchedTemplate; }
    public void setMatchedTemplate(String matchedTemplate) { this.matchedTemplate = matchedTemplate; }
    public String getReferenceUrl() { return referenceUrl; }
    public void setReferenceUrl(String referenceUrl) { this.referenceUrl = referenceUrl; }
    public String getCve() { return cve; }
    public void setCve(String cve) { this.cve = cve; }
    public Double getCvss() { return cvss; }
    public void setCvss(Double cvss) { this.cvss = cvss; }
    public String getRecommendation() { return recommendation; }
    public void setRecommendation(String recommendation) { this.recommendation = recommendation; }
    public Instant getDateFound() { return dateFound; }
    public void setDateFound(Instant dateFound) { this.dateFound = dateFound; }
}
