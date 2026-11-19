package com.example.demo.collabdoc.repository;

import com.example.demo.collabdoc.entity.Notification;
import com.example.demo.collabdoc.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserOrderByCreatedAtDesc(User user);
    List<Notification> findByUserAndIsReadOrderByCreatedAtDesc(User user, Boolean isRead);
    Long countByUserAndIsRead(User user, Boolean isRead);
}
