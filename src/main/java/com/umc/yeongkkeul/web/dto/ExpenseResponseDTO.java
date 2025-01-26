package com.umc.yeongkkeul.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
        private String expenseName;
        private Integer expenseAmount;

        public ExpenseListView2DTO(String expenseName, Integer expenseAmount) {
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
}
