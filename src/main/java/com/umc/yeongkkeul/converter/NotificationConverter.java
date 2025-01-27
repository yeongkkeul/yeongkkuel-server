package com.umc.yeongkkeul.converter;

import com.umc.yeongkkeul.domain.Notification;
import com.umc.yeongkkeul.domain.mapping.NotificationRead;
import com.umc.yeongkkeul.web.dto.NotificationDetailsResponseDto;

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
                    notification.getCreatedAt()
            ));
        }

        // NotificationDetailsResponseDto로 래핑하여 반환
        return new NotificationDetailsResponseDto(details);
    }
}