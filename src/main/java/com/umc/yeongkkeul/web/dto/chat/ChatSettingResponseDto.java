package com.umc.yeongkkeul.web.dto.chat;

import lombok.Builder;

@Builder
public record ChatSettingResponseDto(
        Boolean isDirty
) {
}
