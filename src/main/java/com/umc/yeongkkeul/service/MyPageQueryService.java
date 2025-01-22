package com.umc.yeongkkeul.service;

import com.umc.yeongkkeul.apiPayload.code.status.ErrorStatus;
import com.umc.yeongkkeul.apiPayload.exception.handler.UserHandler;
import com.umc.yeongkkeul.domain.Expense;
import com.umc.yeongkkeul.domain.Reward;
import com.umc.yeongkkeul.domain.User;
import com.umc.yeongkkeul.repository.ExpenseRepository;
import com.umc.yeongkkeul.repository.RewardRepository;
import com.umc.yeongkkeul.repository.UserRepository;
import com.umc.yeongkkeul.web.dto.MyPageInfoResponseDto;
import com.umc.yeongkkeul.web.dto.RewardResponseDto;
import com.umc.yeongkkeul.web.dto.UserReferralCodeResponseDto;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class MyPageQueryService {

    private final UserRepository userRepository;
    private final ExpenseRepository expenseRepository;
    private final RewardRepository rewardRepository;

    // 추천인 코드 조회
    public UserReferralCodeResponseDto getUserReferralCode(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));

        String userReferralCode = user.getReferralCode();
        return new UserReferralCodeResponseDto(userReferralCode);
    }

    // 마이페이지 프로필 조회
    public MyPageInfoResponseDto getUserInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));

        double weeklyAchievementRate = caculateWeeklyAchievementRate(user); // weeklyAchievementRate 계산

        return MyPageInfoResponseDto.of(user, weeklyAchievementRate);
    }

    // weeklyAchievementRate 계산
    public double caculateWeeklyAchievementRate(User user) {
        LocalDate today = LocalDate.now(); // 오늘 날짜
        LocalDate startDay = today.with(DayOfWeek.MONDAY); // 이번주 월요일 날짜
        LocalDate endDay = today.minusDays(1); // 어제 날짜

        if (today.getDayOfWeek() == DayOfWeek.MONDAY) { // 월요일일 때
            startDay = today.minusDays(6); // 지난주 월요일 날짜
            endDay = today.minusDays(1); // 지난주 일요일 날짜
        }

        List<Expense> expenses = expenseRepository.findByUserIdAndCreatedAtBetween(user.getId(), startDay, endDay); // 일주일 지출 데이터
        int achievedDays = 0; // 달성 일수

        for (Expense expense : expenses) {
            LocalDate date = expense.getCreatedAt().toLocalDate();

            int dayExpenditure = 0; // 하루 지출
            for (Expense dailyExpense : expenses) {
                if (dailyExpense.getCreatedAt().toLocalDate().equals(date)) {
                    dayExpenditure += dailyExpense.getAmount();
                }
            }

            if (dayExpenditure <= user.getDayTargetExpenditure()) {
                achievedDays++;
            }
        }
        double weeklyAchievementRate = (achievedDays / ((double) Period.between(startDay, endDay).getDays()+1)) *100;
        return Math.round(weeklyAchievementRate*10) / 10.0;
    }

    // 리워드 목록 조회
    public List<RewardResponseDto> getRewardList(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));


        List<Reward> rewards = rewardRepository.findByUser(user);
        List<RewardResponseDto> rewardList = new ArrayList<>();
        for (Reward reward : rewards) {
            RewardResponseDto dto = RewardResponseDto.from(reward);
            rewardList.add(dto);
        }

        return rewardList;
    }
}
