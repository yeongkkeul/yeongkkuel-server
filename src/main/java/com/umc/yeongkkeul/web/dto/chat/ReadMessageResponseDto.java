package com.umc.yeongkkeul.web.dto.chat;

import lombok.Builder;

import java.util.Map;

@Builder
public record ReadMessageResponseDto(
    Map<Long, Integer> unreadCount
) {
}
