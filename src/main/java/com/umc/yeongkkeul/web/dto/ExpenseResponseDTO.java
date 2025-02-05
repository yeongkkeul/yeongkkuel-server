package com.umc.yeongkkeul.web.dto;

import com.umc.yeongkkeul.domain.enums.AgeGroup;
import com.umc.yeongkkeul.domain.enums.Job;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

public class ExpenseResponseDTO {
    @Builder
    @Getter
    @NoArgsConstructor
    // @AllArgsConstructor
    public static class ExpenseListViewDTO { // expenseId, content, amount -> 지출 생성 때 사용
        private Long expenseId;
        String content;
        private Integer amount;

        public ExpenseListViewDTO(Long expenseId, String content, Integer amount) {
            this.expenseId = expenseId;
            this.content = content;
            this.amount = amount;
        }
    }

    @Builder
    @Getter
    @NoArgsConstructor
    public static class ExpenseListView2DTO { // expenseName, expenseAmount -> 일별 사용자의 카테고리별 지출 기록(목록)조회 때 사용
        private Long expenseId;
        private String expenseName;
        private Integer expenseAmount;

        public ExpenseListView2DTO(Long expenseId, String expenseName, Integer expenseAmount) {
            this.expenseId = expenseId;
            this.expenseName = expenseName;
            this.expenseAmount = expenseAmount;
        }
    }

    @Builder
    @Getter
    @NoArgsConstructor
    public static class ExpensePerDayDTO  { // 주간 - 지출액 조회 때 사용
        private String expenseDate; // "2025-01-11, Saturday"
        private Integer expediture; // 그 날의 총 지출액

        public ExpensePerDayDTO (String expenseDate, Integer expediture) {
            this.expenseDate = expenseDate;
            this.expediture = expediture;
        }
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DayTargetExpenditureViewDTO { // 하루 목표 지출액 조회
        private Integer dayTargetExpenditure;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryListExpenditureViewDTO {
        List<CategoryResponseDTO.CategoryViewListWithExpenditureDTO> categories;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WeeklyExpenditureViewDTO {
        Integer weekExpenditure;
        Integer dayTargetExpenditure;
        List<ExpensePerDayDTO> expenses;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WeeklyAverageExpenditureViewDTO {
        AgeGroup age;
        Job job;
        Integer topPercent;
        Integer averageExpenditure;
        Integer myAverageExpenditure;
        Integer lastWeekExpenditure;
        Integer thisWeekExpenditure;
        String highestExpenditureCategoryName; // 지출액이 가장 큰 카테고리 이름
        List<CategoryResponseDTO.CategoryViewListWithWeeklyExpenditureDTO> categories;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthExpenseDTO {
        Integer year;
        Integer month;
        LocalDate expenseDate;
        Integer expenditure;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyExpenditureViewDTO {
        Integer totalMonthExpenditure; // 한달 지출 누적액
        List<MonthExpenseDTO> selectedMonthExpenses; // 요청 받은 Month
        List<MonthExpenseDTO> previousMonthExpenses; // 요청 받은 Month - 1
        Integer achieveDays;
        Integer rewards;
    }
}
