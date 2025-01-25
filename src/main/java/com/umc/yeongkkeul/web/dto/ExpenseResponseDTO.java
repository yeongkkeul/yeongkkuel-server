package com.umc.yeongkkeul.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class ExpenseResponseDTO {
    @Builder
    @Getter
    @NoArgsConstructor
    // @AllArgsConstructor
    public static class ExpenseListViewDTO {
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
     @AllArgsConstructor
    public static class DayTargetExpenditureViewDTO { // 하루 목표 지출액 조회
        private Integer dayTargetExpenditure;
    }
}
