package com.umc.yeongkkeul.converter;

import com.umc.yeongkkeul.domain.Category;
import com.umc.yeongkkeul.domain.Expense;
import com.umc.yeongkkeul.domain.User;
import com.umc.yeongkkeul.web.dto.ExpenseRequestDTO;

public class ExpenseConverter {
    public static Expense createExpense(ExpenseRequestDTO.ExpenseDTO request, User user, Category category) {
        return Expense.builder()
                .day(request.getDay())
                .content(request.getContent())
                .amount(request.getAmount())
                .isNoSpending(request.getIsExpense())
                .imageUrl(request.getExpenseImg())
                .isSend(request.getSendChatRoom())
                .user(user)
                .category(category)
                .build();
    }



}
