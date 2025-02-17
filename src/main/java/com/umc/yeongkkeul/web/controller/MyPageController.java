package com.umc.yeongkkeul.web.controller;

import com.umc.yeongkkeul.apiPayload.ApiResponse;
import com.umc.yeongkkeul.service.MyPageCommandService;
import com.umc.yeongkkeul.service.MyPageQueryService;
import com.umc.yeongkkeul.web.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static com.umc.yeongkkeul.security.FindLoginUser.getCurrentUserId;
import static com.umc.yeongkkeul.security.FindLoginUser.toId;

@RestController
@RequiredArgsConstructor
@Tag(name = "마이페이지 API", description = "마이페이지 관련 API 입니다.")
public class MyPageController {

    private final MyPageCommandService myPageCommandService;
    private final MyPageQueryService myPageQueryService;

    @Operation(summary = "추천인 코드 조회")
    @GetMapping("/api/userReferralCode")
    public ApiResponse<UserReferralCodeResponseDto> getUserReferralCode() {
        Long userId = toId(getCurrentUserId());
        return ApiResponse.onSuccess(myPageQueryService.getUserReferralCode(userId));
    }

    @Operation(summary = "프로필 조회")
    @GetMapping("/api/mypage")
    public ApiResponse<MyPageInfoResponseDto> getMyPageInfo() {
        Long userId = toId(getCurrentUserId());
        return ApiResponse.onSuccess(myPageQueryService.getUserInfo(userId));
    }

    @Operation(summary = "프로필 수정", description = "multipart/form-data로 보내야 합니다")
    @PatchMapping(value = "/api/mypage", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ApiResponse<MyPageInfoResponseDto> updateUserInfo(@RequestPart("myPageInfoRequestDto") MyPageInfoRequestDto myPageInfoRequestDto,
                                                             @RequestPart(value = "profileImage", required = false) MultipartFile profileImage) {

        Long userId = toId(getCurrentUserId());
        return ApiResponse.onSuccess(myPageCommandService.updateUserInfo(userId, myPageInfoRequestDto, profileImage));
    }

    @Operation(summary = "리워드 목록 조회")
    @GetMapping("/api/rewards")
    public ApiResponse<List<RewardResponseDto>> getRewardList() {
        Long userId = toId(getCurrentUserId());
        return ApiResponse.onSuccess(myPageQueryService.getRewardList(userId));
    }

    @Operation(summary = "하루 목표 지출액 수정")
    @PatchMapping("/api/expenditures/target")
    public ApiResponse<MyPageInfoResponseDto> updateDayTargetExpenditure(@RequestBody ExpenseRequestDTO.DayTargetExpenditureRequestDto dayTargetExpenditureRequestDto) {
        Long userId = toId(getCurrentUserId());

        return ApiResponse.onSuccess(myPageCommandService.updateDayTargetExpenditure(userId, dayTargetExpenditureRequestDto));
    }

    @Operation(summary = "회원 탈퇴")
    @DeleteMapping("/api/auth/delete")
    public ApiResponse<String> deleteUser(@RequestBody UserExitRequestDto userExitRequestDto) {
        Long userId = toId(getCurrentUserId());
        myPageCommandService.deleteUser(userId, userExitRequestDto);
        return ApiResponse.onSuccess("계정 탈퇴 성공입니다.");
    }
}
