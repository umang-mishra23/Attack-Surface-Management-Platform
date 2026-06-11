package com.sentinelasm.asset;

import com.sentinelasm.user.UserService;
import com.sentinelasm.port.OpenPortRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/assets")
public class AssetController {
    private final AssetRepository assets;
    private final OpenPortRepository ports;
    private final UserService userService;

    public AssetController(AssetRepository assets, OpenPortRepository ports, UserService userService) {
        this.assets = assets;
        this.ports = ports;
        this.userService = userService;
    }

    @GetMapping
    public List<Asset> list() {
        return assets.findByOwnerOrderByLastSeenDesc(userService.currentUser());
    }

    @GetMapping("/{id}")
    public AssetDtos.AssetDetail detail(@PathVariable UUID id) {
        Asset asset = assets.findByIdAndOwner(id, userService.currentUser())
                .orElseThrow(() -> new IllegalArgumentException("Asset not found"));
        return new AssetDtos.AssetDetail(asset, ports.findByAsset(asset));
    }
}
