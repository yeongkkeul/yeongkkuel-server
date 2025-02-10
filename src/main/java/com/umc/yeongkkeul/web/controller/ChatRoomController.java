package com.umc.yeongkkeul.web.controller;

import com.umc.yeongkkeul.apiPayload.ApiResponse;
import com.umc.yeongkkeul.service.ChatRoomQueryService;
import com.umc.yeongkkeul.web.dto.BannerResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "채팅 API", description = "채팅 관련 API 입니다.")
public class ChatRoomController {

    private final ChatRoomQueryService chatRoomQueryService;

    @GetMapping("/api/chats/{chatRoomId}/banner")
    @Operation(summary = "채팅방 배너 조회")
    public ApiResponse<BannerResponseDto> getChatRoomBanner(@PathVariable("chatRoomId") Long chatRoomId) {
        return ApiResponse.onSuccess(chatRoomQueryService.getChatRoomBanner(chatRoomId));
    }
}
