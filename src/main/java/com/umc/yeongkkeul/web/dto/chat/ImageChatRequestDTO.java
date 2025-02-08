package com.umc.yeongkkeul.web.dto.chat;

import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

public class ImageChatRequestDTO {
    @Getter
    public static class ImageDTO{
        MultipartFile chatPicture;
    }
}
