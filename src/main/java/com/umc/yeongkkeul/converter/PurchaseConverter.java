package com.umc.yeongkkeul.converter;

import com.umc.yeongkkeul.domain.mapping.Purchase;
import com.umc.yeongkkeul.web.dto.PurchaseResponseDTO;

import java.util.List;
import java.util.stream.Collectors;

public class PurchaseConverter {
    // 구매 이력 단일 -> PurchaseViewDTO로 변환
    public static PurchaseResponseDTO.PurchaseViewDTO toPurchaseViewDTO(Purchase purchase){
        return new PurchaseResponseDTO.PurchaseViewDTO(
                purchase.getItem() != null ? purchase.getItem().getName() : "No Item - Name",
                purchase.getItem() != null ? purchase.getItem().getType().name() : "No Item - Type"
        );
    }

    // 구매 이력 리스트 -> PurchaseViewListDTO로 변환
    public static PurchaseResponseDTO.PurchaseViewListDTO toPurchaseViewListDTO(List<Purchase> purchaseList){
        List<PurchaseResponseDTO.PurchaseViewDTO> purchaseViewList = purchaseList.stream()
                .map(PurchaseConverter::toPurchaseViewDTO)
                .collect(Collectors.toList());
        return new PurchaseResponseDTO.PurchaseViewListDTO(purchaseViewList);
    }
}
