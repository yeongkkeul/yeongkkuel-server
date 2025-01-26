package com.umc.yeongkkeul.service;

import com.umc.yeongkkeul.domain.Expense;
import com.umc.yeongkkeul.web.dto.ExpenseRequestDTO;

public abstract class ExpenseCommandService {
    // 유저의 지출 내역 생성
    public abstract Expense createExpense(String userEmail, String categoryName, ExpenseRequestDTO.ExpenseDTO request);

    // 유저의 지출 내역 수정
    public abstract Expense updateExpense(String userEmail, Long expenseId, String categoryName, ExpenseRequestDTO.ExpenseDTO request);

    // 유저의 지출 내역 삭제
    // public abstract void deleteExpense(Long userId, Long expenseId);
    public abstract void deleteExpense(String userEmail, Long expenseId);
}
