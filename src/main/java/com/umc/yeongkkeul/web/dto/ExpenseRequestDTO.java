package com.umc.yeongkkeul.web.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.time.LocalDate;

public class ExpenseRequestDTO {

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExpenseDTO {
        @NotNull(message = "날짜 작성은 필수입니다.")
        private LocalDate day;

        @NotNull(message = "카테고리 Id 작성은 필수입니다.")
        // private String category;
        private Long categoryId;

        private String content;

        @NotNull(message = "지출 금액 작성은 필수입니다.")
        private Integer amount;

        @NotNull(message = "무지출 여부 작성은 필수입니다.")
        private Boolean isExpense;

        // private String expenseImg;

        @NotNull(message = "채팅방 전송 여부 작성은 필수입니다.")
        private Boolean sendChatRoom;
    }

    @Builder
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExpenseUpdateDTO {
        @NotNull(message = "날짜 작성은 필수입니다.")
        private LocalDate day;

        @NotNull(message = "카테고리 Id 작성은 필수입니다.")
        // private String category;
        private Long categoryId;

        private String content;

        @NotNull(message = "지출 금액 작성은 필수입니다.")
        private Integer amount;

        // private String expenseImg;
    }


    @Builder
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DayTargetExpenditureRequestDto { // 유저의 하루 목표 지출액 설정
        @NotNull(message = "하루 목표 지출액은 필수입니다.")
        @Positive(message = "하루 목표 지출액은 0보다 커야 합니다.")
        private Integer dayTargetExpenditure;
    }
}
