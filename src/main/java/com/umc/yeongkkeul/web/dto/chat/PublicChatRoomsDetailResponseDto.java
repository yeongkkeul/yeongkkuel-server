package com.umc.yeongkkeul.web.dto.chat;

import com.umc.yeongkkeul.domain.ChatRoom;
import lombok.Builder;

import java.text.DecimalFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Builder
public record PublicChatRoomsDetailResponseDto(

        List<PublicChatRoomDetailDto> publicChatRoomDetailDtos
) {

    @Builder
    public record PublicChatRoomDetailDto(
        Long chatRoomId,
        String chatRoomTitle,
        String chatRoomAgeRange,
        String chatRoomJob,
        String chatRoomMaxUserCount,
        Integer chatRoomParticipationCount,
        String chatRoomThumbnail,
        String chatRoomDDay,
        String chatRoomSpendingAmount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Boolean isPassword
    ) {

        public static PublicChatRoomDetailDto of(ChatRoom chatRoom) {

            DecimalFormat formatter = new DecimalFormat("#,###");

            String ddayStr = (chatRoom.getCreatedAt() != null) ? (Duration.between(chatRoom.getCreatedAt(), LocalDateTime.now()).toDays() + 1) + " 일째" : null;
            Integer spendingGoalMoney = chatRoom.getDailySpendingGoalFilter();

            return PublicChatRoomDetailDto.builder()
                    .chatRoomId(chatRoom.getId())
                    .chatRoomTitle(chatRoom.getTitle())
                    .chatRoomAgeRange(chatRoom.getAgeGroupFilter().getAgeGroup())
                    .chatRoomJob(chatRoom.getJobFilter().getJob())
                    .chatRoomMaxUserCount(chatRoom.getMaxParticipants() + " 명")
                    .chatRoomParticipationCount(chatRoom.getParticipationCount())
                    .chatRoomThumbnail(chatRoom.getImageUrl())
                    .chatRoomSpendingAmount(formatter.format(spendingGoalMoney) + " 원")
                    .chatRoomDDay(ddayStr)
                    .createdAt(chatRoom.getCreatedAt())
                    .updatedAt(chatRoom.getUpdatedAt())
                    .isPassword(chatRoom.getPassword() != null)
                    .build();
        }
    }
}
