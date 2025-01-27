package com.umc.yeongkkeul.repository;

import com.umc.yeongkkeul.domain.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
}
