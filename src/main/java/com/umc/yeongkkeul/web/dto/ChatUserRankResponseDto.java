package com.umc.yeongkkeul.web.dto;

import com.umc.yeongkkeul.domain.enums.AgeGroup;
import com.umc.yeongkkeul.domain.enums.Job;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

public class ChatUserRankResponseDto {

    @Builder
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class userInfoDto {
        private Long userId;
        private String nickname;
        private String profileImage;
        private Double rankScore;
        private int rank;
    }

    @Builder
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class chatRankListDto{
        private List<userInfoDto> userRanks;
    }

}
