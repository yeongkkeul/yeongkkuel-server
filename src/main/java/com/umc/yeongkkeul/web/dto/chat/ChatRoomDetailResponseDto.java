package com.umc.yeongkkeul.web.dto.chat;

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
        /*
        Integer participationCount,
        Integer chatRoomMaxUserCount,
        Integer chatRoomSpendingAmountGoal,
        Integer chatRoomAchievedCount,

         */

        String chatRoomChallenger,
        String chatRoomSpendingAmountGoal,
        String chatRoomAchievedCount,
        String chatRoomAverageExpense,
        String chatRoomChallengerGroupRanking,
        String chatRoomImageUrl,
        Boolean isPassword // 해당 채팅방에 패스워드가 있는지
) {
}