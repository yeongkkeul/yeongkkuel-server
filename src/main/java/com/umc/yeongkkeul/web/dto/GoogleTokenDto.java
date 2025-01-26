package com.umc.yeongkkeul.web.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GoogleTokenDto {
    @NotBlank
    private String idToken;
}
