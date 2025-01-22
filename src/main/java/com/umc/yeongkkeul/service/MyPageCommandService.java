package com.umc.yeongkkeul.service;

import com.umc.yeongkkeul.apiPayload.code.status.ErrorStatus;
import com.umc.yeongkkeul.apiPayload.exception.handler.UserHandler;
import com.umc.yeongkkeul.domain.User;
import com.umc.yeongkkeul.repository.UserRepository;
import com.umc.yeongkkeul.web.dto.MyPageInfoResponseDto;
import com.umc.yeongkkeul.web.dto.MyPageInfoRequestDto;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class MyPageCommandService {

    private final UserRepository userRepository;
    private final MyPageQueryService myPageQueryService;

    // TODO: 프로필사진 수정 (string -> 이미지 파일로)
    public MyPageInfoResponseDto updateUserInfo(Long userId, MyPageInfoRequestDto mypageInfoRequestDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));

        user.updateProfile(mypageInfoRequestDto.getNickname(), mypageInfoRequestDto.getGender(),mypageInfoRequestDto.getAgeGroup(), mypageInfoRequestDto.getJob(),mypageInfoRequestDto.getProfileImageUrl());
        userRepository.save(user);

        double weeklyAchievementRate = myPageQueryService.caculateWeeklyAchievementRate(user);
        return MyPageInfoResponseDto.of(user, weeklyAchievementRate);
    }
}
