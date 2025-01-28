package com.umc.yeongkkeul.service;

import com.umc.yeongkkeul.apiPayload.code.status.ErrorStatus;
import com.umc.yeongkkeul.apiPayload.exception.handler.UserHandler;
import com.umc.yeongkkeul.domain.Reward;
import com.umc.yeongkkeul.domain.User;
import com.umc.yeongkkeul.repository.RewardRepository;
import com.umc.yeongkkeul.repository.UserRepository;
import com.umc.yeongkkeul.web.dto.RewardRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class RewardCommandService {

    private final UserRepository userRepository;
    private final RewardRepository rewardRepository;

    public void saveReward(Long userId, RewardRequestDto rewardRequestDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));

        Reward reward = rewardRequestDto.toEntity(user);
        rewardRepository.save(reward);
    }
}
