package com.sentinelasm.port;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sentinelasm.asset.Asset;
import com.sentinelasm.user.AppUser;
import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "open_ports")
public class OpenPort {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JsonIgnore
    private AppUser owner;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JsonIgnore
    private Asset asset;

    private int port;
    private String protocol;
    private String service;
    private String version;
    private String state;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public AppUser getOwner() { return owner; }
    public void setOwner(AppUser owner) { this.owner = owner; }
    public Asset getAsset() { return asset; }
    public void setAsset(Asset asset) { this.asset = asset; }
    public int getPort() { return port; }
    public void setPort(int port) { this.port = port; }
    public String getProtocol() { return protocol; }
    public void setProtocol(String protocol) { this.protocol = protocol; }
    public String getService() { return service; }
    public void setService(String service) { this.service = service; }
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
}
