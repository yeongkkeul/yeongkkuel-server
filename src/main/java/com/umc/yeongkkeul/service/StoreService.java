package com.umc.yeongkkeul.service;

import com.umc.yeongkkeul.apiPayload.code.status.ErrorStatus;
import com.umc.yeongkkeul.apiPayload.exception.GeneralException;
import com.umc.yeongkkeul.domain.Item;
import com.umc.yeongkkeul.domain.User;
import com.umc.yeongkkeul.domain.enums.ItemType;
import com.umc.yeongkkeul.domain.mapping.Purchase;
import com.umc.yeongkkeul.repository.ItemRepository;
import com.umc.yeongkkeul.repository.PurchaseRepository;
import com.umc.yeongkkeul.repository.UserRepository;
import com.umc.yeongkkeul.web.dto.PurchaseRequestDto;
import com.umc.yeongkkeul.web.dto.PurchaseResponseDTO;
import com.umc.yeongkkeul.web.dto.StoreResponseDto;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Data
@RequiredArgsConstructor
public class StoreService {

    @Autowired
    private PurchaseRepository purchaseRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ItemRepository itemRepository;

    //상점 조회
    public StoreResponseDto.StoreViewDto storeView(String email, ItemType itemType){

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new GeneralException(ErrorStatus._USER_NOT_FOUND));

        //착용중인 아이템
        List<PurchaseResponseDTO.PurchaseViewDTO> mySkin = purchaseRepository.findAllByIsUsedAndUser_Email(true, email)
                .stream()
                .map(purchase -> PurchaseResponseDTO.PurchaseViewDTO.builder()
                        .itemName(purchase.getItem().getName())
                        .itemType(purchase.getItem().getType().toString())
                        .build())
                .collect(Collectors.toList());

        List<StoreResponseDto.ItemDto> itemList;

        if(itemType==ItemType.MY){
            itemList = purchaseRepository.findByUser(user)
                    .stream()
                    .map(purchase->StoreResponseDto.ItemDto.builder()
                            .id(purchase.getId())
                            .itemName(purchase.getItem().getName())
                            .price(purchase.getItem().getPrice())
                            .itemImg(purchase.getItem().getImageUrl())
                            .build())
                    .collect(Collectors.toList());


        }else {
            //상정 타입 별
            itemList = itemRepository.findAllByTypeOrderByCreatedAt(itemType)
                    .stream()
                    .map(item -> StoreResponseDto.ItemDto.builder()
                            .id(item.getId())
                            .itemName(item.getName())
                            .price(item.getPrice())
                            .itemImg(item.getImageUrl())
                            .build())
                    .collect(Collectors.toList());
        }

        // StoreViewDto 생성 및 반환
        return StoreResponseDto.StoreViewDto.builder()
                .myReward(user.getRewardBalance())
                .mySkin(mySkin)
                .itemType(itemType)
                .itemList(itemList)
                .build();
    }


    //스킨 구매
    public void purchaseItem(String email, PurchaseRequestDto.PurchaseItemInfo purchaseItemInfo){

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new GeneralException(ErrorStatus._USER_NOT_FOUND));

        Item item = itemRepository.findById(purchaseItemInfo.getItemId())
                .orElseThrow(()-> new GeneralException(ErrorStatus._ITEM_NOT_FOUND));

        boolean processionItem = purchaseRepository.existsByUserAndItem_Id(user,purchaseItemInfo.getItemId());

        //차감
        if(user.getRewardBalance()>=item.getPrice()){
            user.setRewardBalance(user.getRewardBalance()-item.getPrice());
            userRepository.save(user);
        }else{
            throw new GeneralException(ErrorStatus._NOT_ENOUGH_REWARD);
        }


        if(!processionItem){

            Purchase purchase = Purchase.builder()
                    .user(user)
                    .item(item)
                    .purchasedAt(LocalDateTime.now())
                    .usedReward(item.getPrice())
                    .isUsed(false)
                    .type(purchaseItemInfo.getItemType())
                    .build();

            purchaseRepository.save(purchase);
        }

    }

    //스킨 착용
    public void wearItem(String email, PurchaseRequestDto.WearingItemDto wearingItemInfo){

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new GeneralException(ErrorStatus._USER_NOT_FOUND));

        wearingItemInfo.getUserItem().forEach(purchaseItem -> {
            Purchase purchase = purchaseRepository.findByIdAndUser(purchaseItem.getPurchaseId(), user)
                    .orElseThrow(() -> new GeneralException(ErrorStatus._PURCHASE_NOT_FOUND));
            purchase.setIsUsed(true);
            purchaseRepository.save(purchase);
        });


    }


}
