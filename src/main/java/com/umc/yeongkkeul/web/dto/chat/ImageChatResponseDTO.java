package com.umc.yeongkkeul.web.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ImageChatResponseDTO {
    private Long messageId;
    private String imageUrl;
}