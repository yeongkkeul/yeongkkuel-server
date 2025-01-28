package com.umc.yeongkkeul.web.dto;

import com.umc.yeongkkeul.domain.enums.ItemType;
import lombok.*;

import java.util.List;

public class StoreResponseDto {

    @Builder
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItemDto{
        private Long id;
        private String itemName;
        private Integer price;
        private String itemImg;

    }



    @Builder
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StoreViewDto{
        private Integer myReward;
        private List<PurchaseResponseDTO.PurchaseViewDTO> mySkin;
        private ItemType itemType;
        private List<ItemDto> itemList;


    }



}
