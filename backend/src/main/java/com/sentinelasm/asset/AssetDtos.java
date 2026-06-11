package com.sentinelasm.asset;

import com.sentinelasm.port.OpenPort;

import java.util.List;

public class AssetDtos {
    public record AssetDetail(Asset asset, List<OpenPort> openPorts) {}
}
