package com.umc.yeongkkeul.web.controller;

import com.umc.yeongkkeul.apiPayload.ApiResponse;
import com.umc.yeongkkeul.service.MyPageQueryService;
import com.umc.yeongkkeul.web.dto.UserReferralCodeResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "MyPage", description = "마이페이지 관련 API")
public class MyPageController {

    private final MyPageQueryService myPageQueryService;

    @Operation(summary = "추천인 코드 조회")
    @GetMapping("/api/userReferralCode")
    public ApiResponse<UserReferralCodeResponseDto> getUserReferralCode(@RequestParam Long userId) {
        return ApiResponse.onSuccess(myPageQueryService.getUserReferralCode(userId));
    }
}
