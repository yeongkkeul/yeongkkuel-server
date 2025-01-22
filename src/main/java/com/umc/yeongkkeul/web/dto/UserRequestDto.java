package com.umc.yeongkkeul.web.dto;

import com.umc.yeongkkeul.domain.enums.AgeGroup;
import com.umc.yeongkkeul.domain.enums.Job;
import lombok.*;

public class UserRequestDto {

    @Builder
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class userInfoDto {
        private String nickName;
        private String gender;
        private AgeGroup ageGroup;
        private Job job;
        private String referralCode;


    }

    @Builder
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TermDTO {
        private String term1;
        private String term2;
        private String term3;
        private String term4;

    }
}
