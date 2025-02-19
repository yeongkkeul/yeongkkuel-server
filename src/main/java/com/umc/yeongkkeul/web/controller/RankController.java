package com.umc.yeongkkeul.web.controller;


import com.umc.yeongkkeul.apiPayload.ApiResponse;
import com.umc.yeongkkeul.service.UserRankService;
import com.umc.yeongkkeul.web.dto.ChatUserProfileDto;
import com.umc.yeongkkeul.web.dto.ChatUserRankResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api")
@Tag(name = "랭크 API", description = "랭크 관련 API 입니다.")
public class RankController {

    @Autowired
    private UserRankService userRankService;


    @GetMapping("/chats/{chatRoomId}/ranks")
    @Operation(description = "채팅방 내 사용자 랭킹", summary = "채팅방 내 사용자 랭킹 userScore 내림차순")
    public ApiResponse<ChatUserRankResponseDto.chatRankListDto> chatRankList(@PathVariable Long chatRoomId) {

        ChatUserRankResponseDto.chatRankListDto chatRankList = userRankService.chatRankListDto(chatRoomId);

        return ApiResponse.onSuccess(chatRankList);
    }

    @GetMapping("/chats/{chatRoomId}/user/{userId}")
    @Operation(description = "채팅방 사용자 프로필 조회", summary = "채팅방 내 사용자 프로필 조회")
    public ApiResponse<ChatUserProfileDto> chatUserProfile(@PathVariable Long chatRoomId, @PathVariable Long userId) {

        ChatUserProfileDto chatUserProfile = userRankService.chatUserProfile(chatRoomId, userId);

        return ApiResponse.onSuccess(chatUserProfile);
    }

}
