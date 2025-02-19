package com.umc.yeongkkeul.repository;

import com.umc.yeongkkeul.domain.mapping.NotificationRead;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

public interface NotificationReadRepository extends JpaRepository<NotificationRead, Long> {

    @Query("SELECT n FROM NotificationRead n WHERE n.userId.id = :userId ORDER BY n.createdAt DESC")
    Page<NotificationRead> findAllByUserIDOrderByCreatedAtDesc(@Param("userId") Long userId,
                                                         Pageable pageable);

    @Modifying
    @Query("UPDATE NotificationRead n SET n.isRead = true WHERE n.userId.id = :userId AND n.isRead = false")
    int setNotificationUnreadToRead(@Param("userId") Long userId);

    @Query("SELECT COUNT(n) > 0 FROM NotificationRead n WHERE n.userId.id = :userId AND n.isRead = false")
    boolean existsByUserId(@Param("userId") Long userId);

    // "하루 목표 지출액 초과" 알림이 오늘 날짜에 존재하는지 확인
    @Query("SELECT CASE WHEN COUNT(nr) > 0 THEN TRUE ELSE FALSE END FROM NotificationRead nr " +
            "WHERE nr.userId.id = :userId " +
            "AND nr.notificationId.notificationType = 'EXCEED_DAILY_SPENDING_GOAL' " +
            "AND DATE(nr.createdAt) = :today")
    boolean existsExceedDailySpendingGoalNotification(@Param("userId") Long userId, @Param("today") LocalDate today);
}