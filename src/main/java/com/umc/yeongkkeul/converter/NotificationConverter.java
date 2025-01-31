package com.umc.yeongkkeul.converter;

import com.umc.yeongkkeul.domain.Notification;
import com.umc.yeongkkeul.domain.mapping.NotificationRead;
import com.umc.yeongkkeul.web.dto.NotificationDetailsResponseDto;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class NotificationConverter {

    /**
     * @param notifications 알림 테이블
     * @param notificationReads 알림-유저 연관 테이블
     * @return 알림과 알림-유저 연관 테이블을 NotificationDetailsResponseDto로 변환
     */
    public static NotificationDetailsResponseDto toNotificationsDto(List<Notification> notifications, List<NotificationRead> notificationReads) {

        if (notificationReads.size() != notifications.size()) {
            throw new IllegalArgumentException("Notification reads and notifications list sizes must match.");
        }

        // NotificationDetail 리스트 생성
        List<NotificationDetailsResponseDto.NotificationDetail> details = new ArrayList<>();
        for (int i = 0; i < notifications.size(); i++) {

            Notification notification = notifications.get(i);
            NotificationRead notificationRead = notificationReads.get(i);

            // NotificationDetail 객체 생성하여 리스트에 추가
            details.add(new NotificationDetailsResponseDto.NotificationDetail(
                    notification.getId(),
                    notification.getNotificationType().toString(), // enum을 String으로 변환
                    notification.getNotificationContent(),
                    notification.getTargetUrl(),
                    notificationRead.getIsRead(),
                    createdatToTimestamp(notification.getCreatedAt()),
                    notification.getCreatedAt()
            ));
        }

        // NotificationDetailsResponseDto로 래핑하여 반환
        return new NotificationDetailsResponseDto(details);
    }

    /**
     * @param createdAt 생성일
     * @return LocalDateTime 타입의 생성일을 주어진 기준에 따라 String으로 변환한다.
     */
    public static String createdatToTimestamp(LocalDateTime createdAt) {

        LocalDateTime localDateTime = LocalDateTime.now();

        Duration duration = Duration.between(createdAt, localDateTime);

        if (duration.toMinutes() < 1) {
            // 1분 이하 차이
            return duration.toSeconds() + "초 전";
        } else {
            if (duration.toHours() < 1) {
                // 1시간 이하 차이
                return duration.toMinutes() + "분 전";
            } else if (duration.toHours() < 24) {
                // 1시간 이상 24시간 이하 차이
                return duration.toHours() + "시간 전";
            } else if (duration.toHours() < 48) {
                // 24시간 이상 48시간 이하 차이
                return createdAt.format(DateTimeFormatter.ofPattern("HH:mm"));
            } else {
                // 48시간 이상 차이
                return createdAt.format(DateTimeFormatter.ofPattern("MM/dd"));
            }
        }
    }
}