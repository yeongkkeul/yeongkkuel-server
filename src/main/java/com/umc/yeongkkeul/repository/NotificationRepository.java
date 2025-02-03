package com.umc.yeongkkeul.repository;

import com.umc.yeongkkeul.domain.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Query("SELECT n FROM Notification n WHERE n.id IN :notificationIds ORDER BY n.createdAt DESC")
    List<Notification> findAllByIdIn(@Param("notificationIds") List<Long> notificationIds);
}