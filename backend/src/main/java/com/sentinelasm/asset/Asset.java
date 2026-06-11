package com.sentinelasm.asset;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sentinelasm.scan.Scan;
import com.sentinelasm.user.AppUser;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "assets")
public class Asset {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JsonIgnore
    private AppUser owner;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JsonIgnore
    private Scan scan;

    private String rootDomain;
    private String subdomain;
    private String ipAddress;
    private String status;
    private Integer responseCode;
    private String pageTitle;
    private int riskScore;
    private String screenshotPath;
    private Instant firstSeen = Instant.now();
    private Instant lastSeen = Instant.now();

    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> technologies = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> dnsRecords = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> httpHeaders = new ArrayList<>();

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public AppUser getOwner() { return owner; }
    public void setOwner(AppUser owner) { this.owner = owner; }
    public Scan getScan() { return scan; }
    public void setScan(Scan scan) { this.scan = scan; }
    public String getRootDomain() { return rootDomain; }
    public void setRootDomain(String rootDomain) { this.rootDomain = rootDomain; }
    public String getSubdomain() { return subdomain; }
    public void setSubdomain(String subdomain) { this.subdomain = subdomain; }
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getResponseCode() { return responseCode; }
    public void setResponseCode(Integer responseCode) { this.responseCode = responseCode; }
    public String getPageTitle() { return pageTitle; }
    public void setPageTitle(String pageTitle) { this.pageTitle = pageTitle; }
    public int getRiskScore() { return riskScore; }
    public void setRiskScore(int riskScore) { this.riskScore = riskScore; }
    public String getScreenshotPath() { return screenshotPath; }
    public void setScreenshotPath(String screenshotPath) { this.screenshotPath = screenshotPath; }
    public Instant getFirstSeen() { return firstSeen; }
    public void setFirstSeen(Instant firstSeen) { this.firstSeen = firstSeen; }
    public Instant getLastSeen() { return lastSeen; }
    public void setLastSeen(Instant lastSeen) { this.lastSeen = lastSeen; }
    public List<String> getTechnologies() { return technologies; }
    public void setTechnologies(List<String> technologies) { this.technologies = technologies; }
    public List<String> getDnsRecords() { return dnsRecords; }
    public void setDnsRecords(List<String> dnsRecords) { this.dnsRecords = dnsRecords; }
    public List<String> getHttpHeaders() { return httpHeaders; }
    public void setHttpHeaders(List<String> httpHeaders) { this.httpHeaders = httpHeaders; }
}
