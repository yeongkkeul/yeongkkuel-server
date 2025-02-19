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

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
                        .imgUrl(purchase.getImageUrl())
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
                            .itemImg(purchase.getItem().getImgUrl())
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

        String itemName = item.getImgUrl();


        String regex = "Store(.*?)\\.png";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(itemName);

        String itemNameWithoutEncoding = "";
        if (matcher.find()) {
            itemNameWithoutEncoding = matcher.group(1); // Extract the matched part without encoding
        }

        // Construct the final image URL with the item name without encoding
        String baseUrl = "https://yeongkkeul-s3.s3.ap-northeast-2.amazonaws.com/store-item/";
        String imageUrl = baseUrl + "Home" + itemNameWithoutEncoding + ".png";  // 최종 URL


        if(!processionItem){

            Purchase purchase = Purchase.builder()
                    .user(user)
                    .item(item)
                    .purchasedAt(LocalDateTime.now())
                    .usedReward(item.getPrice())
                    .isUsed(false)
                    .type(purchaseItemInfo.getItemType())
                    .imageUrl(imageUrl)
                    .build();

            purchaseRepository.save(purchase);
        }

    }

    //스킨 착용
    public void wearItem(String email, PurchaseRequestDto.WearingItemDto wearingItemInfo){

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new GeneralException(ErrorStatus._USER_NOT_FOUND));

        // 해당 유저의 모든 구매 아이템 가져오기
        List<Purchase> allPurchases = purchaseRepository.findByUser(user);

        // 착용할 아이템의 purchaseId 리스트 추출
        Set<Long> wearingItemIds = wearingItemInfo.getUserItem().stream()
                .map(PurchaseRequestDto.PurchaseItem::getPurchaseId)
                .collect(Collectors.toSet());

        // 모든 아이템의 isUsed를 false로 설정하고, 착용할 아이템만 true로 변경
        allPurchases.forEach(purchase -> {
            if (wearingItemIds.contains(purchase.getId())) {
                purchase.setIsUsed(true);  // 착용한 아이템이면 true
            } else {
                purchase.setIsUsed(false); // 나머지는 false
            }
        });

        // 변경 사항을 한 번에 저장
        purchaseRepository.saveAll(allPurchases);

    }


}
