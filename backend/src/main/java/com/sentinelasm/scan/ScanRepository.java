package com.sentinelasm.scan;

import com.sentinelasm.domain.ScanStatus;
import com.sentinelasm.user.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ScanRepository extends JpaRepository<Scan, UUID> {
    List<Scan> findByOwnerOrderByCreatedAtDesc(AppUser owner);
    Optional<Scan> findByIdAndOwner(UUID id, AppUser owner);
    long countByOwner(AppUser owner);
    long countByOwnerAndStatus(AppUser owner, ScanStatus status);
}
