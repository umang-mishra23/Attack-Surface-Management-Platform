package com.sentinelasm.notification;

import com.sentinelasm.user.AppUser;
import com.sentinelasm.user.UserService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {
    private final NotificationRepository notifications;
    private final UserService userService;

    public NotificationService(NotificationRepository notifications, UserService userService) {
        this.notifications = notifications;
        this.userService = userService;
    }

    public void create(AppUser owner, String title, String message, String type) {
        Notification notification = new Notification();
        notification.setOwner(owner);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notifications.save(notification);
    }

    public List<Notification> listCurrentUser() {
        return notifications.findByOwnerOrderByCreatedAtDesc(userService.currentUser());
    }
}
