package com.umc.yeongkkeul.web.dto.chat;

import java.util.List;

public record ReadMessageRequestDto(
        List<Long> messageList
) {
}
