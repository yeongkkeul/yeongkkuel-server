package com.umc.yeongkkeul.web.dto;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public record NotificationDetailsResponseDto(

    List<NotificationDetail> notificationDetails
) {

    @Getter
    public record NotificationDetail(
            Long id,
            String notificationType,
            String notificationContent,
            String targetUrl,
            Boolean isRead,
            LocalDateTime createdAt
    ) {
    }
}
