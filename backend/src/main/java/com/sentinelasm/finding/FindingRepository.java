package com.sentinelasm.finding;

import com.sentinelasm.domain.Severity;
import com.sentinelasm.scan.Scan;
import com.sentinelasm.user.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FindingRepository extends JpaRepository<Finding, UUID> {
    List<Finding> findByOwnerOrderByDateFoundDesc(AppUser owner);
    List<Finding> findByScan(Scan scan);
    long countByOwner(AppUser owner);
    long countByOwnerAndSeverity(AppUser owner, Severity severity);
}
