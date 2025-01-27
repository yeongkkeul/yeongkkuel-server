package com.umc.yeongkkeul.web.dto;

import com.umc.yeongkkeul.domain.enums.ItemType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

public class PurchaseRequestDto {

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PurchaseItemInfo{
        Long itemId;
        ItemType itemType;
        String itemName;
        int reward;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PurchaseItem{
        Long purchaseId;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WearingItemDto{
        List<PurchaseItem> userItem;
    }
}
