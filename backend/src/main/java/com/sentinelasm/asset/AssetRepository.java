package com.sentinelasm.asset;

import com.sentinelasm.scan.Scan;
import com.sentinelasm.user.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AssetRepository extends JpaRepository<Asset, UUID> {
    List<Asset> findByOwnerOrderByLastSeenDesc(AppUser owner);
    List<Asset> findByScan(Scan scan);
    Optional<Asset> findByIdAndOwner(UUID id, AppUser owner);
    long countByOwner(AppUser owner);
    long countByOwnerAndStatus(AppUser owner, String status);
}
