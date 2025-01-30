package com.umc.yeongkkeul.web.dto.chat;

import lombok.Builder;

@Builder
public record ReceiptMessageDto(
        String senderName,
        String category,
        String content,
        Integer amount,
        String imageUrl,
        Boolean isNoSpending
) {
}
