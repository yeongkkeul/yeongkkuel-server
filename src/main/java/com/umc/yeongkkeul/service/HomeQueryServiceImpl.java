package com.umc.yeongkkeul.service;

import com.umc.yeongkkeul.apiPayload.code.status.ErrorStatus;
import com.umc.yeongkkeul.apiPayload.exception.handler.PurchaseHandler;
import com.umc.yeongkkeul.apiPayload.exception.handler.UserHandler;
import com.umc.yeongkkeul.converter.CategoryConverter;
import com.umc.yeongkkeul.converter.PurchaseConverter;
import com.umc.yeongkkeul.domain.Category;
import com.umc.yeongkkeul.domain.User;
import com.umc.yeongkkeul.domain.mapping.Purchase;
import com.umc.yeongkkeul.repository.CategoryRepository;
import com.umc.yeongkkeul.repository.ExpenseRepository;
import com.umc.yeongkkeul.repository.PurchaseRepository;
import com.umc.yeongkkeul.repository.UserRepository;
import com.umc.yeongkkeul.web.dto.CategoryResponseDTO;
import com.umc.yeongkkeul.web.dto.ExpenseResponseDTO;
import com.umc.yeongkkeul.web.dto.HomeResponseDTO;
import com.umc.yeongkkeul.web.dto.PurchaseResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class HomeQueryServiceImpl implements HomeQueryService {

    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final PurchaseRepository purchaseRepository;

    @Override
    public HomeResponseDTO.HomeViewDTO viewHome(String userEmail){
        // 오늘의 날짜는요
        LocalDate today = java.time.LocalDate.now();

        // 유저 찾기
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));

        // 유저의 아이템 구매 이력 가져오기
        List<Purchase> purchases = purchaseRepository.findByUser(user);

        // isUsed = true인 항목만 필터링
        List<Purchase> filteredPurchases = purchases.stream()
                .filter(purchase -> purchase.getIsUsed() == true)
                .collect(Collectors.toList());

        // 구매 이력을 DTO로 변환
        List<PurchaseResponseDTO.PurchaseViewDTO> usingItemList
                = PurchaseConverter.toPurchaseViewListDTO(filteredPurchases).getPurchaseList();

        // 카테고리 및 지출 정보 가져오기
        List<Category> categories = categoryRepository.findAllByUserId(user.getId());
        List<CategoryResponseDTO.CategoryViewListWithHomeDTO> categoryList
                = CategoryConverter.toCategoriesViewListWithHomeDTO(categories, user, today);

        return HomeResponseDTO.HomeViewDTO.builder()
                .myReward(user.getRewardBalance())
                .mySkin(usingItemList)
                .today(today)
                .categories(categoryList)
                .build();
    }
}
