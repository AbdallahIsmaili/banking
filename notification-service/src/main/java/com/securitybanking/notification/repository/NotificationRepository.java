package com.securitybanking.notification.repository;


import com.securitybanking.notification.model.Notification;
import com.securitybanking.notification.model.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
      */
    List<Notification> findByClientIdOrderBySentAtDesc(String clientId);

    /**
     * Find notifications by client ID and status, ordered by sent date
     */
    List<Notification> findByClientIdAndStatusOrderBySentAtDesc(String clientId, NotificationStatus status);

    /**
     * Count notifications by client ID and status
     */
    long countByClientIdAndStatus(String clientId, NotificationStatus status);

    /**
     * Find notifications sent after a specific date
     */
    List<Notification> findByClientIdAndSentAtAfterOrderBySentAtDesc(String clientId, LocalDateTime since);

    /**
     * Find notifications by recipient
     */
    List<Notification> findByRecipientOrderBySentAtDesc(String recipient);
}