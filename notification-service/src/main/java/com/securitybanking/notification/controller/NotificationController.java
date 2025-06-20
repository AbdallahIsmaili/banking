package com.securitybanking.notification.controller;

import com.securitybanking.notification.dto.NotificationRequest;
import com.securitybanking.notification.model.Notification;
import com.securitybanking.notification.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    /**
     * Create a new notification when an account is created
     * This replaces the email sending functionality
     */
    @PostMapping("/account-created/{clientId}")
    public ResponseEntity<Notification> accountCreated(@PathVariable String clientId,
            @RequestParam String accountNumber) {
        try {
            Notification notification = notificationService.createNotification(
                    clientId,
                    accountNumber,
                    "Account Created Successfully",
                    "Welcome! Your account has been created successfully.");
            return ResponseEntity.ok(notification);
        } catch (Exception e) {
            System.err.println("Error creating notification: " + e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Create a generic notification
     */
    @PostMapping
    public ResponseEntity<Notification> createNotification(@RequestBody NotificationRequest request) {
        try {
            Notification notification = notificationService.createNotification(
                    request.getClientId(),
                    request.getRecipient(),
                    request.getSubject() != null ? request.getSubject() : "Notification",
                    request.getMessage());
            return ResponseEntity.ok(notification);
        } catch (Exception e) {
            System.err.println("Error creating notification: " + e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<Notification>> getNotifications(@PathVariable String clientId) {
        try {
            List<Notification> notifications = notificationService.getNotificationsForClient(clientId);
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Get all notifications (for testing)
     */
    @GetMapping
    public ResponseEntity<List<Notification>> getAllNotifications() {
        try {
            List<Notification> notifications = notificationService.getAllNotifications();
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Get unread notifications count for a client
     */
    @GetMapping("/client/{clientId}/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(@PathVariable String clientId) {
        try {
            long count = notificationService.getUnreadCount(clientId);
            return ResponseEntity.ok(Map.of("unreadCount", count));
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Get unread notifications for a client
     */
    @GetMapping("/client/{clientId}/unread")
    public ResponseEntity<List<Notification>> getUnreadNotifications(@PathVariable String clientId) {
        try {
            List<Notification> notifications = notificationService.getUnreadNotifications(clientId);
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Mark a specific notification as read
     */
    @PutMapping("/{notificationId}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long notificationId) {
        try {
            notificationService.markAsRead(notificationId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Mark all notifications as read for a client
     */
    @PutMapping("/client/{clientId}/mark-all-read")
    public ResponseEntity<Void> markAllAsRead(@PathVariable String clientId) {
        try {
            notificationService.markAllAsRead(clientId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
}