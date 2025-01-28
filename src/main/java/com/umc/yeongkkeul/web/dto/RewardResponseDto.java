package com.umc.yeongkkeul.web.dto;

import com.umc.yeongkkeul.domain.Reward;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class RewardResponseDto {

    private LocalDateTime datetime;
    private String record;
    private String type;
    private int reward;

    public static RewardResponseDto from(Reward reward) {
        return RewardResponseDto.builder()
                .datetime(reward.getCreatedAt())
                .record("INDIVIDUAL".equals(reward.getRewardType().toString()) ? "개인 목표 달성" : "TEAM".equals(reward.getRewardType().toString()) ? "팀 목표 달성" : null)
                .type(reward.getDescription())
                .reward(reward.getAmount())
                .build();
    }
}
