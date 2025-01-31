package com.umc.yeongkkeul.web.dto.chat;

import lombok.Builder;

/**
 * 생성방 가입/생성 후에 반환되는 DTO
 */
@Builder
public record ChatRoomResponseDto(

        Long chatRoomId,
        String chatRoomTitle,
        String lastActivity,
        String lastMessage,
        Integer participationCount,
        String chatRoomImageUrl,
        Integer unreadChatCount
) {
}
