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
        private Boolean term1;
        private Boolean term2;
        private Boolean term3;
        private Boolean term4;
    }
}
