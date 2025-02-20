package com.umc.yeongkkeul.web.dto.chat;

public record ReadMessageRequestDto(
        String lastClientMessageId,
        String recentClientMessageId
) {
}
