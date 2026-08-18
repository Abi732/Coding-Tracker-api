package com.abi.coding_tracker.notification.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.abi.coding_tracker.entity.User;
import com.abi.coding_tracker.notification.entity.Notification;
import com.abi.coding_tracker.notification.entity.NotificationStatus;
import com.abi.coding_tracker.notification.entity.NotificationType;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    List<Notification> findAllByUser(User user);
    List<Notification> findAllByStatus(NotificationStatus status);
    List<Notification> findAllByUserOrderByCreatedAtDesc(User user);

    List<Notification> findAllByType(NotificationType type);
    List<Notification> findTop20ByUserOrderByCreatedAtDesc(User user);
    List<Notification> findAllByStatusAndRetryCountLessThan(NotificationStatus status, int maxRetries);
}
