package com.umc.yeongkkeul.web.dto.chat;

import lombok.Builder;

@Builder
public record ReadMessageResponseDto(
    String startMessageId,
    String endMessageId
) {
}
