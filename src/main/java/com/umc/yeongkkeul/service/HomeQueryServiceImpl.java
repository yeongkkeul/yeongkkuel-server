package com.umc.yeongkkeul.service;

import com.umc.yeongkkeul.apiPayload.code.status.ErrorStatus;
import com.umc.yeongkkeul.apiPayload.exception.handler.PurchaseHandler;
import com.umc.yeongkkeul.apiPayload.exception.handler.UserHandler;
import com.umc.yeongkkeul.converter.CategoryConverter;
import com.umc.yeongkkeul.converter.PurchaseConverter;
import com.umc.yeongkkeul.domain.Category;
import com.umc.yeongkkeul.domain.Reward;
import com.umc.yeongkkeul.domain.User;
import com.umc.yeongkkeul.domain.mapping.Purchase;
import com.umc.yeongkkeul.repository.*;
import com.umc.yeongkkeul.web.dto.CategoryResponseDTO;
import com.umc.yeongkkeul.web.dto.ExpenseResponseDTO;
import com.umc.yeongkkeul.web.dto.HomeResponseDTO;
import com.umc.yeongkkeul.web.dto.PurchaseResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
    private final RewardRepository rewardRepository;

    @Override
    public HomeResponseDTO.HomeViewDTO viewHome(Long userId){
        // 오늘의 날짜는요
        LocalDate today = java.time.LocalDate.now();

        // 유저 찾기
        User user = userRepository.findById(userId)
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

    @Override
    public HomeResponseDTO.YesterdayRewardViewDTO yesterdayRewardView(Long userId){
        // 어제 날짜
        LocalDate yesterday = LocalDate.now().minusDays(1);

        // 유저 찾기
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));

        // 어제 획득한 리워드 목록 가져오기
        List<Reward> rewards = rewardRepository.findByUserIdAndCreatedAtDate(userId, yesterday);

        // 어제 획득한 리워드 총합 계산
        Integer yesterdayReward = rewards.stream()
                .mapToInt(Reward::getAmount)
                .sum();

        return HomeResponseDTO.YesterdayRewardViewDTO.builder()
                .yesterdayReward(yesterdayReward)
                .build();
    }
}
