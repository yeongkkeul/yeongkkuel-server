package com.umc.yeongkkeul.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record NotificationDetailRequestDto(
        @NotNull String notificationType,
        @NotBlank String content,
        String targetUrl
) {
}