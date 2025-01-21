package com.umc.yeongkkeul.web.controller;

import com.umc.yeongkkeul.apiPayload.ApiResponse;
import com.umc.yeongkkeul.service.UserCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Validated
public class UserController {

    private final UserCommandService userCommandService;

    @DeleteMapping("/{userId}")
    public ApiResponse<?> deleteUser(@PathVariable Long userId) {
        userCommandService.deleteUser(userId);

        return ApiResponse.builder()
                .isSuccess(true)
                .code("2000")
                .message("해당 유저와 연관된 데이터가 삭제되었습니다.")
                .build();
    }
}