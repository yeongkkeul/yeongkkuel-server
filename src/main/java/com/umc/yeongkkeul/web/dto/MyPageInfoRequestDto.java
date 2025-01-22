package com.umc.yeongkkeul.web.dto;

import com.umc.yeongkkeul.domain.enums.AgeGroup;
import com.umc.yeongkkeul.domain.enums.Job;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class MyPageInfoRequestDto {
    private String nickname;
    private String gender;
    private AgeGroup ageGroup;
    private Job job;
    private String profileImageUrl;
}
