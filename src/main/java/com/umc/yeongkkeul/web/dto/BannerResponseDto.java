package com.umc.yeongkkeul.web.dto;

import com.umc.yeongkkeul.domain.ChatRoom;
import com.umc.yeongkkeul.domain.enums.AgeGroup;
import com.umc.yeongkkeul.domain.enums.Job;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BannerResponseDto {

    private Integer achievingCount; // 목표 달성 챌린저 수
    private Integer chatRoomUserCount; // 채팅방 내 챌린저 수
    private Integer avgAmount; // 채팅방의 지출 평균
    private String age; // 나이대
    private String job; // 직업대
    private Integer topRate; // 상위 백분위
    private String createdAt; // 공지 날짜

    public static BannerResponseDto from(ChatRoom chatRoom, String createdAt) {
        return BannerResponseDto.builder()
                .achievingCount(chatRoom.getAchievedCount())
                .chatRoomUserCount(chatRoom.getParticipationCount())
                .avgAmount(chatRoom.getAverageExpense())
                .age(chatRoom.getAgeGroupFilter().getAgeGroup())
                .job(chatRoom.getJobFilter().getJob())
                .topRate((chatRoom.getAgeGroupFilter()==AgeGroup.UNDECIDED && chatRoom.getJobFilter()==Job.UNDECIDED) ? null : chatRoom.getRanking())
                .createdAt(createdAt)
                .build();
    }
}
