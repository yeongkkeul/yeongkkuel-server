package com.umc.yeongkkeul.service;

import com.umc.yeongkkeul.apiPayload.code.status.ErrorStatus;
import com.umc.yeongkkeul.apiPayload.exception.handler.NotificationReadHandler;
import com.umc.yeongkkeul.apiPayload.exception.handler.UserHandler;
import com.umc.yeongkkeul.converter.NotificationConverter;
import com.umc.yeongkkeul.domain.Notification;
import com.umc.yeongkkeul.domain.User;
import com.umc.yeongkkeul.domain.enums.NotificationType;
import com.umc.yeongkkeul.domain.mapping.NotificationRead;
import com.umc.yeongkkeul.repository.NotificationReadRepository;
import com.umc.yeongkkeul.repository.NotificationRepository;
import com.umc.yeongkkeul.repository.UserRepository;
import com.umc.yeongkkeul.web.dto.NotificationAgreedRequestDto;
import com.umc.yeongkkeul.web.dto.NotificationDetailRequestDto;
import com.umc.yeongkkeul.web.dto.NotificationDetailsResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationReadRepository notificationReadRepository;
    private final UserRepository userRepository;

    private final int NOTIFICATION_PAGING_SIZE = 30; // 한 페이지 당 최대 30개를 조회

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

    /**
     * 1. NotificationRead 테이블에서 최신 순으로 알림 ID를 가져온다.
     * 2. 그 알림 ID로 Nofication 정보를 가져온다.
     *
     * @param userId 로그인한 사용자 ID
     * @param page 페이지 수
     * @return 알림 목록
     */
    public NotificationDetailsResponseDto getNotifications(Long userId, int page) {

        // NotificationRead 테이블에서 최신 순으로 알림 ID를 가져온다.
        Pageable pageable = PageRequest.of(page, NOTIFICATION_PAGING_SIZE); // 한 페이지 당 최대 30개를 가져온다.
        Page<NotificationRead> notificationPageByUserId = notificationReadRepository.findAllByUserIDOrderByCreatedAtDesc(userId, pageable);
        List<NotificationRead> contents = notificationPageByUserId.getContent();
        List<Long> notificationIds = contents.stream()
                .map(notification -> notification.getId())
                .collect(Collectors.toList());

        // 알림 ID 목록을 기준으로 알림 목록 가져오기
        List<Notification> notifications = notificationRepository.findAllByIdIn(notificationIds);

        // 알림 목록 Dto 생성
        return NotificationConverter.toNotificationsDto(notifications, contents);
    }

    /**
     * 사용자의 알림 수신 여부를 저장한다.
     *
     * @param userId 로그인한 사용자 ID
     * @param notificationAgreedRequestDto 알림 수신 여부
     * @return 알림 수신 여부
     */
    public Boolean setNotificationAgreed(Long userId, NotificationAgreedRequestDto notificationAgreedRequestDto) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserHandler(ErrorStatus._USER_NOT_FOUND));

        user.setNotificationAgreed(notificationAgreedRequestDto.notificationAgreed());
        userRepository.save(user);

        return notificationAgreedRequestDto.notificationAgreed();
    }

    public Boolean isUnreadNotifications(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserHandler(ErrorStatus._USER_NOT_FOUND));

        return notificationReadRepository.existsByUserId(userId);
    }

    public Boolean raedNotification(Long notificationId) {

        NotificationRead notification = notificationReadRepository.findById(notificationId)
                .orElseThrow(() -> new NotificationReadHandler(ErrorStatus._NOTIFICATION_NOT_FOUND));

        notification.setIsRead(true);

        notificationReadRepository.save(notification);

        return true;
    }

    @Transactional
    public Integer raedAllNotifications(Long userId) {

        return notificationReadRepository.setNotificationUnreadToRead(userId);
    }
}