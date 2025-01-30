package com.umc.yeongkkeul.web.dto;

import lombok.Builder;

/**
 * 채팅방 정보
 */
@Builder
public record ChatRoomDetailResponseDto(

        String chatRoomTitle,
        String lastActivity,
        String chatRoomAgeRange,
        String chatRoomJob,
        String createdDaysElapsed,
        Integer participationCount,
        Integer chatRoomMaxUserCount,
        Integer chatRoomSpendingAmountGoal,
        // TODO: 목표 달성 챌린저
        // TODO: 지출 평균
        // TODO: 챌린저 그룹 랭킹
        String chatRoomImageUrl,
        Boolean isPassword // 해당 채팅방에 패스워드가 있는지
) {
}