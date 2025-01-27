package com.umc.yeongkkeul.web.dto;

import jakarta.validation.constraints.NotBlank;

public record NotificationAgreedRequestDto(
        @NotBlank Boolean notificationAgreed
) {
}
