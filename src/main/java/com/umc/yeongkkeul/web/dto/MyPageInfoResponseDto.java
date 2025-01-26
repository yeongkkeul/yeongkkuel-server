package com.umc.yeongkkeul.web.dto;

import com.umc.yeongkkeul.domain.User;
import com.umc.yeongkkeul.domain.enums.AgeGroup;
import com.umc.yeongkkeul.domain.enums.Job;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MyPageInfoResponseDto {

    private String nickname;
    private String gender;
    private Job job;
    private AgeGroup ageGroup;
    private String email;
    private String profileImageUrl;
    private Integer dayTargetExpenditure;
    private int rewardBalance;
    private double weeklyAchievementRate;

    public static MyPageInfoResponseDto of(User user, double weeklyAchievementRate) {
        return MyPageInfoResponseDto.builder()
                .nickname(user.getNickname())
                .gender(user.getGender())
                .job(user.getJob())
                .ageGroup(user.getAgeGroup())
                .email(user.getEmail())
                .profileImageUrl(user.getImageUrl())
                .dayTargetExpenditure(user.getDayTargetExpenditure())
                .rewardBalance(user.getRewardBalance())
                .weeklyAchievementRate(weeklyAchievementRate)
                .build();
    }
}
