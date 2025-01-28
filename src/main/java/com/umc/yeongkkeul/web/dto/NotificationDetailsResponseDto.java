package com.umc.yeongkkeul.web.dto;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

public record NotificationDetailsResponseDto(

    List<NotificationDetail> notificationDetails
) {

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