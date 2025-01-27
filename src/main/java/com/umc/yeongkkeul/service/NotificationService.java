package com.umc.yeongkkeul.service;

import com.umc.yeongkkeul.apiPayload.code.status.ErrorStatus;
import com.umc.yeongkkeul.apiPayload.exception.handler.UserHandler;
import com.umc.yeongkkeul.domain.Notification;
import com.umc.yeongkkeul.domain.User;
import com.umc.yeongkkeul.domain.enums.NotificationType;
import com.umc.yeongkkeul.domain.mapping.NotificationRead;
import com.umc.yeongkkeul.repository.NotificationReadRepository;
import com.umc.yeongkkeul.repository.NotificationRepository;
import com.umc.yeongkkeul.repository.UserRepository;
import com.umc.yeongkkeul.web.dto.NotificationDetailRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationReadRepository notificationReadRepository;
    private final UserRepository userRepository;

    /**
     * 알림과 연관 테이블을 저장
     *
     * @param userId 로그인한 사용자 ID
     * @param notificationDetailRequestDto 알림 정보
     * @return 생성한 알림 ID
     */
    @Transactional
    public Long createNotification(Long userId, NotificationDetailRequestDto notificationDetailRequestDto) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserHandler(ErrorStatus._USER_NOT_FOUND));

        // 알림 저장
        Notification notification = Notification.builder()
                .notificationType(NotificationType.valueOf(notificationDetailRequestDto.notificationType()))
                .notificationContent(notificationDetailRequestDto.content())
                .targetUrl(notificationDetailRequestDto.targetUrl())
                .build();

        Notification savedNotification = notificationRepository.save(notification);

        // 알림-사용자 관계 테이블 저장
        NotificationRead notificationRead = NotificationRead.builder()
                .userId(user)
                .notificationId(savedNotification)
                .isRead(false)
                .build();

        notificationReadRepository.save(notificationRead);

        return savedNotification.getId();
    }
}
