package com.sentinelasm.notification;

import com.sentinelasm.user.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    List<Notification> findByOwnerOrderByCreatedAtDesc(AppUser owner);
}
