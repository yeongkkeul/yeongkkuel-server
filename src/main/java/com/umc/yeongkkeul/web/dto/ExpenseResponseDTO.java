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
        private Integer amount;

        public ExpenseListViewDTO(Long expenseId, Integer amount) {
            this.expenseId = expenseId;
            this.amount = amount;
        }
    }
}
