package com.umc.yeongkkeul.web.dto.chat;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

public record ChatRoomDetailRequestDto(
        @NotBlank String chatRoomTitle,
        String chatRoomPassword,
        @Max(99999999) @NotNull Integer chatRoomSpendingAmountGoal,
        @Max(100) @Min(0) @NotNull Integer chatRoomMaxUserCount,
        String chatRoomAgeRange,
        String chatRoomJob,
        @Size(max = 200) String chatRoomRule
) {
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatRoomUpdateDTO{
        @NotBlank String chatRoomName;
        String chatRoomPassword;
        @Max(99999999) @NotNull Integer chatRoomSpendingAmountGoal;
        @Max(100) @Min(0) @NotNull Integer chatRoomMaxUserCount;
    }
}