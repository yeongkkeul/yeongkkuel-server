package com.umc.yeongkkeul.web.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

public class ExpenseRequestDTO {

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExpenseDTO {
        // 이 부분은 유저 로그인 로직 완성 후에 수정 예정
        // @NotNull(message = "userID는 필수입니다.")
        // private Long userId;  // userId 추가

        @NotNull(message = "날짜 작성은 필수입니다.")
        private LocalDate day;
//        private LocalDateTime day;

        @NotNull(message = "카테고리 작성은 필수입니다.")
        private String category;

        private String content;

        @NotNull(message = "지출 금액 작성은 필수입니다.")
        private Integer amount;

        @NotNull(message = "무지출 여부 작성은 필수입니다.")
        private Boolean isExpense;

        private String expenseImg;

        @NotNull(message = "채팅방 전송 여부 작성은 필수입니다.")
        private Boolean sendChatRoom;
    }
}
