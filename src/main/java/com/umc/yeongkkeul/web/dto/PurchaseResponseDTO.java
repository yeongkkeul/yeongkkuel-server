package com.umc.yeongkkeul.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

public class PurchaseResponseDTO {
    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PurchaseViewDTO{
        String itemName;
        String itemType;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PurchaseViewListDTO{
        List<PurchaseViewDTO> purchaseList;
    }
}
