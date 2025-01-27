package com.umc.yeongkkeul.repository;

import com.umc.yeongkkeul.domain.mapping.NotificationRead;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationReadRepository extends JpaRepository<NotificationRead, Long> {

    @Query("SELECT n FROM NotificationRead n WHERE n.userId = :userId ORDER BY n.createdAt DESC")
    Page<NotificationRead> findAllByUserIDOrderByCreatedAtDesc(@Param("userId") Long userId,
                                                         Pageable pageable);
}
