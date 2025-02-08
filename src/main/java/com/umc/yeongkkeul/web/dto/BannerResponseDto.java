package com.umc.yeongkkeul.web.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BannerResponseDto {

    private int achievingCount;
    private int chatRoomUserCount;
    private int avgAmount;
    private String age;
    private String job;
    private Double topRate;
    private String createdAt;
}
