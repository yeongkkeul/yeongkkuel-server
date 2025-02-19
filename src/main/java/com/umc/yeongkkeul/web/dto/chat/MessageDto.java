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
        String timestamp, // 타임스탬프
        Integer unreadCount, // 안읽은 사용자 수,
        Boolean rabbitMQTransmissionStatus, // RabbitMQ로 전송을 완료했는지
        Boolean finalTransmissionStatus, // 최종 메시지 전송 여부
        Boolean saveStatus // Redis 저장 여부
) {
}