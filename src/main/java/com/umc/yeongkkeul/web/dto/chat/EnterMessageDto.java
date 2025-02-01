package com.umc.yeongkkeul.web.dto.chat;

import java.time.LocalDateTime;

/**
 * MessageDto와 동일하나 패스워드 확인을 위해 패스워드 필드 하나가 더 있다.
 * Redis에 이 메시지를 저장하기 위해서는 패스워드 필드를 버린 채 MessageDto로 변환해서 저장해야됨.
 *
 */
public record EnterMessageDto(
        Long id, // 메시지 ID
        Long chatRoomId, // 목적지(전달할 그룹 채팅방) ID
        Long senderId, // 발신인 ID
        String messageType, // 메시지 타입(텍스트, 사진, (영수증))
        String content, // 메시지 내용
        LocalDateTime timestamp, // 타임스탬프,
        String password // 패스워드
) {
}