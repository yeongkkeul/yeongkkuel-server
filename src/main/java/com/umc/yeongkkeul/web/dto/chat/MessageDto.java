package com.umc.yeongkkeul.web.dto.chat;

import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 채팅 메시지 정보
 * MessageDto를 사용해서 Redis 접근
 */
@Builder
public record MessageDto(
        Long id, // 메시지 ID
        Long chatRoomId, // 목적지(전달할 그룹 채팅방) ID
        Long senderId, // 발신인 ID
        String messageType, // 메시지 타입(텍스트, 사진, 영수증)
        String content, // 메시지 내용
        LocalDateTime timestamp // 타임스탬프
        // TODO: Message 전송 여부, 저장 여부를 저장하는 필드가 있으면 좋을듯
) {
}