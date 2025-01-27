package com.umc.yeongkkeul.web.controller;


import com.umc.yeongkkeul.apiPayload.ApiResponse;
import com.umc.yeongkkeul.domain.enums.ItemType;
import com.umc.yeongkkeul.security.FindLoginUser;
import com.umc.yeongkkeul.service.StoreService;
import com.umc.yeongkkeul.web.dto.PurchaseRequestDto;
import com.umc.yeongkkeul.web.dto.StoreResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api")
public class StoreController {

    @Autowired
    private StoreService storeService;


    @GetMapping("/shop")
    @Operation(summary = "착용 스킨 + 보유 리워드 + 상점 뷰",description = "itemType으로 상점 및 MY(자기가 보유한 스킨 확인)")
    public ApiResponse<StoreResponseDto.StoreViewDto> storeView(@RequestParam ItemType itemType){
        String email = FindLoginUser.getCurrentUserId();

        StoreResponseDto.StoreViewDto storeViewDto = storeService.storeView(email, itemType);

        return ApiResponse.onSuccess(storeViewDto);
    }

    @PostMapping
    @Operation(summary = "스킨 구매", description="스킨 요청 값은 requestBody를 통해 확인")
    public ApiResponse<String> buySkin(@RequestBody PurchaseRequestDto.PurchaseItemInfo purchaseItemInfo){
        String email = FindLoginUser.getCurrentUserId();

        storeService.purchaseItem(email, purchaseItemInfo);

        return ApiResponse.onSuccess("스킨 구매 성공");
    }

    @PutMapping
    @Operation(summary = "스킨 착용 저장", description="착용한 스킨의 구매 내역 id(list 형태로 보내주시면 됩니다.)")
    public ApiResponse<String> wearSkin(@RequestBody PurchaseRequestDto.WearingItemDto wearingItemDto){
        String email = FindLoginUser.getCurrentUserId();

        storeService.wearItem(email, wearingItemDto);

        return ApiResponse.onSuccess("스킨 착용 완료(저장)");
    }

}
