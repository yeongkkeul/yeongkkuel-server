package com.umc.yeongkkeul.web.dto;

import com.umc.yeongkkeul.domain.Reward;
import com.umc.yeongkkeul.domain.User;
import com.umc.yeongkkeul.domain.enums.RewardType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class RewardRequestDto {

    private RewardType rewardType;
    private String description;
    private int amount;

    public Reward toEntity(User user) {
        return Reward.builder()
                .user(user)
                .rewardType(this.rewardType)
                .description(this.description)
                .amount(this.amount)
                .build();
    }
}
