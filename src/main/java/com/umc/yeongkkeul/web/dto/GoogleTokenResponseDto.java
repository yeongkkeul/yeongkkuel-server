package com.umc.yeongkkeul.web.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GoogleTokenResponseDto {
    private String sub;
    private String email;
    private String name;
    private String aud;
    private String iss;
    private Boolean email_verified;
}
