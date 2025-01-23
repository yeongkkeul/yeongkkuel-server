package com.umc.yeongkkeul.web.controller;

import com.umc.yeongkkeul.apiPayload.ApiResponse;
import com.umc.yeongkkeul.service.HomeQueryServiceImpl;
import com.umc.yeongkkeul.web.dto.HomeResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.umc.yeongkkeul.security.FindLoginUser.getCurrentUserId;

@RestController
@RequiredArgsConstructor
@Tag(name = "홍 API", description = "홈 화면 API 입니다.")
@RequestMapping("/api/home")
public class HomeController {

    private final HomeQueryServiceImpl homeQueryServiceImpl;

    @Operation(summary = "홈 화면 조회")
    @GetMapping("/")
    public ApiResponse<HomeResponseDTO.HomeViewDTO> home() {
        String userEmail = getCurrentUserId();
        return ApiResponse.onSuccess(homeQueryServiceImpl.viewHome(userEmail));
    }
}
