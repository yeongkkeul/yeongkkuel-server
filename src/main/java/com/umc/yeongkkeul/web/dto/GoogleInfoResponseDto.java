package com.umc.yeongkkeul.web.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class GoogleInfoResponseDto {
    private String sub;
    private String email;
    private String name;
}
