package com.umc.yeongkkeul.web.dto.chat;

import com.umc.yeongkkeul.domain.ChatRoom;
import lombok.Builder;

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
        Integer chatRoomMaxUserCount,
        Integer chatRoomParticipationCount,
        String chatRoomThumbnail,
        String chatRoomDDay,
        Integer chatRoomSpendingAmount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Boolean isPassword
    ) {

        public static PublicChatRoomDetailDto of(ChatRoom chatRoom) {

            String ddayStr = (chatRoom.getCreatedAt() != null) ? (Duration.between(chatRoom.getCreatedAt(), LocalDateTime.now()).toDays() + 1) + " 일째" : null;

            return PublicChatRoomDetailDto.builder()
                    .chatRoomId(chatRoom.getId())
                    .chatRoomTitle(chatRoom.getTitle())
                    .chatRoomAgeRange(String.valueOf(chatRoom.getAgeGroupFilter()))
                    .chatRoomJob(String.valueOf(chatRoom.getJobFilter()))
                    .chatRoomMaxUserCount(chatRoom.getMaxParticipants())
                    .chatRoomParticipationCount(chatRoom.getParticipationCount())
                    .chatRoomThumbnail(chatRoom.getImageUrl())
                    .chatRoomSpendingAmount(chatRoom.getDailySpendingGoalFilter())
                    .chatRoomDDay(ddayStr)
                    .createdAt(chatRoom.getCreatedAt())
                    .updatedAt(chatRoom.getUpdatedAt())
                    .isPassword(chatRoom.getPassword() != null)
                    .build();
        }
    }
}
