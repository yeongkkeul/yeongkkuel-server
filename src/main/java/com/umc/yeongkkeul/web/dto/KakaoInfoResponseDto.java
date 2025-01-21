package com.umc.yeongkkeul.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class KakaoInfoResponseDto {

    @Builder
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KakaoInfoDTO{
        String accessToken;
        String refreshToken;
        String email;
        String redirectUrl;

    }


}
