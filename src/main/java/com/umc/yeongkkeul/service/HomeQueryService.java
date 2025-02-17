package com.umc.yeongkkeul.service;

import com.umc.yeongkkeul.web.dto.HomeResponseDTO;

public interface HomeQueryService {
    // 홈 화면 조회
    HomeResponseDTO.HomeViewDTO viewHome(Long userId);

    // 전날 획득한 리워드 조회
    HomeResponseDTO.YesterdayRewardViewDTO yesterdayRewardView(Long userId);
}
