package com.umc.yeongkkeul.web.dto;

import jakarta.validation.constraints.*;

public record ChatRoomDetailRequestDto(
        @NotBlank String chatRoomTitle,
        String chatRoomPassword,
        @Max(99999999) @NotNull Integer chatRoomSpendingAmountGoal,
        @Max(100) @Min(0) @NotNull Integer chatRoomMaxUserCount,
        String chatRoomAgeRange,
        String chatRoomJob,
        @Size(max = 200) String chatRoomRule,
        String chatRoomImageUrl
) {
}