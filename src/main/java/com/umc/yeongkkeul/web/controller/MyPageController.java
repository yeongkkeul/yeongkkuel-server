package com.umc.yeongkkeul.web.controller;

import com.umc.yeongkkeul.apiPayload.ApiResponse;
import com.umc.yeongkkeul.service.MyPageCommandService;
import com.umc.yeongkkeul.service.MyPageQueryService;
import com.umc.yeongkkeul.web.dto.MyPageInfoRequestDto;
import com.umc.yeongkkeul.web.dto.MyPageInfoResponseDto;
import com.umc.yeongkkeul.web.dto.RewardResponseDto;
import com.umc.yeongkkeul.web.dto.UserReferralCodeResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "MyPage", description = "마이페이지 관련 API")
public class MyPageController {

    // TODO: api 전부 다 토큰으로 사용자 인증으로 변경

    private final MyPageCommandService myPageCommandService;
    private final MyPageQueryService myPageQueryService;

    @Operation(summary = "추천인 코드 조회")
    @GetMapping("/api/userReferralCode")
    public ApiResponse<UserReferralCodeResponseDto> getUserReferralCode(@RequestParam Long userId) {
        return ApiResponse.onSuccess(myPageQueryService.getUserReferralCode(userId));
    }

    @Operation(summary = "프로필 조회")
    @GetMapping("/api/mypage")
    public ApiResponse<MyPageInfoResponseDto> getMyPageInfo(@RequestParam Long userId) {
        return ApiResponse.onSuccess(myPageQueryService.getUserInfo(userId));
    }

    @Operation(summary = "프로필 수정")
    @PatchMapping("/api/mypage")
    public ApiResponse<MyPageInfoResponseDto> updateUserInfo(@RequestParam Long userId, @RequestBody MyPageInfoRequestDto myPageInfoRequestDto) {
        return ApiResponse.onSuccess(myPageCommandService.updateUserInfo(userId, myPageInfoRequestDto));
    }

    @Operation(summary = "리워드 목록 조회")
    @GetMapping("/api/rewards")
    public ApiResponse<List<RewardResponseDto>> getRewardList(@RequestParam Long userId) {
        return ApiResponse.onSuccess(myPageQueryService.getRewardList(userId));
    }
}
