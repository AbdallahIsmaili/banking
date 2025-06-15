package com.securitybanking.notification.service;


import com.securitybanking.notification.model.Notification;
import com.securitybanking.notification.model.NotificationStatus;
import com.securitybanking.notification.model.NotificationType;
import com.securitybanking.notification.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    /**
     * Create a new notification and store it in the database
     * This replaces the email sending functionality
     */
    public Notification createNotification(String clientId, String recipient,
                                           String subject, String message) {
        Notification notification = new Notification();
        notification.setClientId(clientId);
        notification.setRecipient(recipient);
        notification.setSubject(subject);
        notification.setMessage(message);
        notification.setType(NotificationType.IN_APP);
        notification.setStatus(NotificationStatus.SENT);
        notification.setSentAt(LocalDateTime.now());

        return notificationRepository.save(notification);
    }

    /**
     * Get all notifications for a specific client
     */
    public List<Notification> getNotificationsForClient(String clientId) {
        return notificationRepository.findByClientIdOrderBySentAtDesc(clientId);
    }

    /**
     * Get unread notifications for a client
     */
    public List<Notification> getUnreadNotifications(String clientId) {
        return notificationRepository.findByClientIdAndStatusOrderBySentAtDesc(
                clientId, NotificationStatus.SENT);
    }

    /**
     * Mark a specific notification as read
     */
    public void markAsRead(Long notificationId) {
        Optional<Notification> notification = notificationRepository.findById(notificationId);
        if (notification.isPresent()) {
            notification.get().setStatus(NotificationStatus.READ);
            notification.get().setReadAt(LocalDateTime.now());
            notificationRepository.save(notification.get());
        }
    }

    /**
     * Mark all notifications as read for a client
     */
    public void markAllAsRead(String clientId) {
        List<Notification> unreadNotifications = getUnreadNotifications(clientId);
        unreadNotifications.forEach(notification -> {
            notification.setStatus(NotificationStatus.READ);
            notification.setReadAt(LocalDateTime.now());
        });
        notificationRepository.saveAll(unreadNotifications);
    }

    /**
     * Get count of unread notifications
     */
    public long getUnreadCount(String clientId) {
        return notificationRepository.countByClientIdAndStatus(clientId, NotificationStatus.SENT);
    }

    /**
     * Get all notifications (for testing purposes)
     */
    public List<Notification> getAllNotifications() {
        return notificationRepository.findAll();
    }
}