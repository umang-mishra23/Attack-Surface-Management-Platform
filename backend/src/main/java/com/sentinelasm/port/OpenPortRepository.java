package com.sentinelasm.port;

import com.sentinelasm.asset.Asset;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OpenPortRepository extends JpaRepository<OpenPort, UUID> {
    List<OpenPort> findByAsset(Asset asset);
}
