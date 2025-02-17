package com.umc.yeongkkeul.converter;

import com.umc.yeongkkeul.domain.ChatRoom;
import com.umc.yeongkkeul.domain.User;
import com.umc.yeongkkeul.domain.enums.AgeGroup;
import com.umc.yeongkkeul.domain.enums.Job;
import com.umc.yeongkkeul.domain.mapping.ChatRoomMembership;
import com.umc.yeongkkeul.web.dto.chat.ChatRoomDetailRequestDto;
import com.umc.yeongkkeul.web.dto.chat.ChatRoomDetailResponseDto;

import java.text.DecimalFormat;
import java.time.Duration;
import java.time.LocalDateTime;

public class ChatRoomConverter {

    /**
     * @param chatRoomDetailRequestDto 채팅방 생성 정보
     * @return 채팅방 생성 정보 DTO를 ChatRoom 객체로 변환
     */
    public static ChatRoom toChatRoomEntity(ChatRoomDetailRequestDto chatRoomDetailRequestDto) {

        return ChatRoom.builder()
                .title(chatRoomDetailRequestDto.chatRoomTitle())
                .password(chatRoomDetailRequestDto.chatRoomPassword())
                .description(chatRoomDetailRequestDto.chatRoomRule())
                .maxParticipants(chatRoomDetailRequestDto.chatRoomMaxUserCount())
                .participationCount(1)
                .ageGroupFilter(AgeGroup.valueOf(chatRoomDetailRequestDto.chatRoomAgeRange()))
                .jobFilter(Job.valueOf(chatRoomDetailRequestDto.chatRoomJob()))
                .dailySpendingGoalFilter(chatRoomDetailRequestDto.chatRoomSpendingAmountGoal())
                .imageUrl(chatRoomDetailRequestDto.chatRoomImageUrl())
                .build();
    }

    /**
     * @param user
     * @param chatRoom
     * @return 채팅방-사용자 테이블 저장을 위한 convert 메서드로 User와 ChatRoom 객체를 받아서 ChatRoomMembership Entity로 변환
     */
    public static ChatRoomMembership toChatRoomMembershipEntity(User user, ChatRoom chatRoom, boolean isHost, Long joinMessageId) {

        return ChatRoomMembership.builder()
                .user(user)
                .chatroom(chatRoom)
                .isHost(isHost)
                .isBanned(false)
                .joinedAt(LocalDateTime.now())
                .userScore((double) 0)
                .joinMessageId(joinMessageId)
                .build();
    }

    /**
     * @param chatRoom
     * @param lastActivity 마지막 활동 전 시간 문자열
     * @return 채팅방 정보 조회를 위한 convert 메서드로 ChatRoom과 기타 정보를 ChatRoomDetailResponseDto로 변환.
     */
    public static ChatRoomDetailResponseDto toChatRoomDetailResponseDto(ChatRoom chatRoom, String lastActivity) {

        DecimalFormat df = new DecimalFormat("#,###");
        LocalDateTime localDateTime = LocalDateTime.now();
        String daysStr = (chatRoom.getCreatedAt() != null) ? (Duration.between(chatRoom.getCreatedAt(), localDateTime).toDays() + 1) + "일 째" : null;
        String groupRanking = "NON";

        if (chatRoom.getAgeGroupFilter() != null && chatRoom.getJobFilter() != null) {
            groupRanking = chatRoom.getAgeGroupFilter() + " " + chatRoom.getJobFilter() + " 상위 " + chatRoom.getRanking() + "%";
        } else if (chatRoom.getAchievedCount() == null && chatRoom.getJobFilter() != null) {
            groupRanking = chatRoom.getJobFilter() + " 상위 " + chatRoom.getRanking() + "%";
        } else if (chatRoom.getAgeGroupFilter() != null && chatRoom.getJobFilter() == null) {
            groupRanking = chatRoom.getAgeGroupFilter() + " 상위 " + chatRoom.getRanking() + "%";
        } else {
            groupRanking = "전체 상위 " + chatRoom.getRanking() + "%";
        }

        return ChatRoomDetailResponseDto.builder()
                .chatRoomTitle(chatRoom.getTitle())
                .lastActivity(lastActivity)
                .chatRoomAgeRange(String.valueOf(chatRoom.getAgeGroupFilter()))
                .chatRoomJob(String.valueOf(chatRoom.getJobFilter()))
                .createdDaysElapsed(daysStr)
                .chatRoomChallenger(chatRoom.getParticipationCount() + "/" + chatRoom.getMaxParticipants())
                .chatRoomSpendingAmountGoal(df.format(chatRoom.getDailySpendingGoalFilter()) + "원")
                .chatRoomAchievedCount(chatRoom.getAchievedCount() + "/" + chatRoom.getParticipationCount())
                .chatRoomAverageExpense(df.format(chatRoom.getAverageExpense()) + "원")
                .chatRoomChallengerGroupRanking(groupRanking)
                .chatRoomImageUrl(chatRoom.getImageUrl())
                .isPassword(chatRoom.getPassword() != null)
                .build();
    }
}