package com.umc.yeongkkeul.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

public class HomeResponseDTO {
    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HomeViewDTO{
        Integer myReward;
        List<PurchaseResponseDTO.PurchaseViewDTO> mySkin;
        LocalDate today;
        List<CategoryResponseDTO.CategoryViewListWithHomeDTO> categories;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class YesterdayRewardViewDTO{
        Integer yesterdayReward;
    }
}
