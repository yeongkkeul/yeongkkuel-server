package com.umc.yeongkkeul.web.controller;

import com.umc.yeongkkeul.apiPayload.ApiResponse;
import com.umc.yeongkkeul.service.NotificationService;
import com.umc.yeongkkeul.web.dto.NotificationAgreedRequestDto;
import com.umc.yeongkkeul.web.dto.NotificationDetailRequestDto;
import com.umc.yeongkkeul.web.dto.NotificationDetailsResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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
     * @return 로그인한 사용자의 알림을 생성
     */
    @PostMapping
    @Operation(summary = "알림 생성", description = "로그인한 사용자의 알림을 생성합니다.")
    public ApiResponse<Long> createNotification(@RequestBody @Valid NotificationDetailRequestDto notificationDetailRequestDto) {

        Long userId = toId(getCurrentUserId());

        // DTO가 유효하지 않으면 BindException 또는 MethodArgumentNotValidException이 발생
        return ApiResponse.onSuccess(notificationService.createNotification(userId, notificationDetailRequestDto));
    }

    /**
     * @param page 알림 페이지
     * @return 로그인한 사용자 알림 목록
     */
    @GetMapping
    @Operation(summary = "알림 목록 조회", description = "로그인한 사용자의 알림 목록을 조회합니다.")
    public ApiResponse<NotificationDetailsResponseDto> getNotifications(@RequestParam(defaultValue = "0") int page) {

        Long userId = toId(getCurrentUserId());

        return ApiResponse.onSuccess(notificationService.getNotifications(userId, page));
    }

    @PatchMapping("/settings")
    @Operation(summary = "알림 수신 설정 변경", description = "사용자가 알림의 수신 여부를 선택할 수 있습니다.")
    public ApiResponse<Boolean> setNotificationAgreed(@RequestBody @Valid NotificationAgreedRequestDto notificationAgreedRequestDto) {

        Long userId = toId(getCurrentUserId());

        return ApiResponse.onSuccess(notificationService.setNotificationAgreed(userId, notificationAgreedRequestDto));
    }

    // TODO: 읽지 않은 알림이 있는지 확인
}
