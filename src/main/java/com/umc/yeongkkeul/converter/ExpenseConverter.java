package com.umc.yeongkkeul.converter;

import com.umc.yeongkkeul.domain.Category;
import com.umc.yeongkkeul.domain.Expense;
import com.umc.yeongkkeul.domain.User;
import com.umc.yeongkkeul.service.ExpenseQueryServiceImpl;
import com.umc.yeongkkeul.web.dto.ExpenseRequestDTO;
import com.umc.yeongkkeul.web.dto.ExpenseResponseDTO;

import java.time.LocalDate;
import java.util.List;

public class ExpenseConverter {
    public static Expense createExpense(ExpenseRequestDTO.ExpenseDTO request, User user, Category category, boolean isExpense) {

        Integer amount = request.getAmount();

        // is_no_spending(무지출 여부)가 true이면 지출 0원
        if (isExpense == true) {
            amount = 0;
        }

        return Expense.builder()
                .day(request.getDay())
                .content(request.getContent())
                .amount(amount)
                .isNoSpending(request.getIsExpense())
                // .imageUrl(request.getExpenseImg())
                .isSend(request.getSendChatRoom())
                .user(user)
                .category(category)
                .build();
    }
}
