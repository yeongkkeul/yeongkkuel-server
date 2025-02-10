package com.umc.yeongkkeul.web.dto.chat;

import com.umc.yeongkkeul.domain.ChatRoom;
import lombok.Builder;

@Builder
public record ChatRoomInfoResponseDto(
        Long chatRoomId,
        String chatRoomTitle,
        String chatRoomThumbnail,
        String chatRoomRule,
        Integer participationCount
) {

    public static ChatRoomInfoResponseDto of(ChatRoom chatRoom) {

        return ChatRoomInfoResponseDto.builder()
                .chatRoomId(chatRoom.getId())
                .chatRoomTitle(chatRoom.getTitle())
                .chatRoomThumbnail(chatRoom.getImageUrl())
                .chatRoomRule(chatRoom.getDescription())
                .participationCount(chatRoom.getParticipationCount())
                .build();
    }
}
