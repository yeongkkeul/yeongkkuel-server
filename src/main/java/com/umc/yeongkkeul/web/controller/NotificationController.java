package com.umc.yeongkkeul.web.controller;

import com.umc.yeongkkeul.apiPayload.ApiResponse;
import com.umc.yeongkkeul.service.NotificationService;
import com.umc.yeongkkeul.web.dto.NotificationDetailRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.umc.yeongkkeul.security.FindLoginUser.getCurrentUserId;
import static com.umc.yeongkkeul.security.FindLoginUser.toId;

@Slf4j
@RestController
@Validated
@RequiredArgsConstructor
@Tag(name = "알림 API", description = "알림 관련 API 입니다.")
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * @param notificationDetailRequestDto 생성 할 알림 정보
     * @return 특정 사용자의 알림을 생성
     */
    @PostMapping
    @Operation(summary = "알림 생성", description = "특정 사용자의 알림을 생성합니다.")
    public ApiResponse<Long> createNotification(@RequestBody @Valid NotificationDetailRequestDto notificationDetailRequestDto) {

        Long userId = toId(getCurrentUserId());

        // DTO가 유효하지 않으면 BindException 또는 MethodArgumentNotValidException이 발생
        return ApiResponse.onSuccess(notificationService.createNotification(userId, notificationDetailRequestDto));
    }
}
