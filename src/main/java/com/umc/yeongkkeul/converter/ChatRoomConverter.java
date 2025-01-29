package com.umc.yeongkkeul.converter;

import com.umc.yeongkkeul.domain.ChatRoom;
import com.umc.yeongkkeul.domain.User;
import com.umc.yeongkkeul.domain.enums.AgeGroup;
import com.umc.yeongkkeul.domain.enums.Job;
import com.umc.yeongkkeul.domain.mapping.ChatRoomMembership;
import com.umc.yeongkkeul.web.dto.ChatRoomDetailRequestDto;

import java.time.LocalDateTime;

public class ChatRoomConverter {

    /**
     * @param chatRoomDetailRequestDto 채팅방 생성 정보
     * @return 채팅방 생성 정보 DTO를 ChatRoom 객체로 변환
     */
    public static ChatRoom toChatRoomEntity(ChatRoomDetailRequestDto chatRoomDetailRequestDto) {

        return ChatRoom.builder()
                .title(chatRoomDetailRequestDto.chatRoomName())
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

    public static ChatRoomMembership toChatRoomMembershipEntity(User user, ChatRoom chatRoom) {

        return ChatRoomMembership.builder()
                .user(user)
                .chatroom(chatRoom)
                .isHost(true)
                .isBanned(false)
                .joinedAt(LocalDateTime.now())
                .build();
    }
}