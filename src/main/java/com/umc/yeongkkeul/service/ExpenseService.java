package com.umc.yeongkkeul.service;

import com.umc.yeongkkeul.domain.Expense;
import com.umc.yeongkkeul.web.dto.ExpenseRequestDTO;

public abstract class ExpenseService {
    // 유저의 지출 내역 생성
    public abstract Expense createExpense(Long userId, String categoryName, ExpenseRequestDTO.ExpenseDTO request);

    // 유저의 지출 내역 수정
    public abstract Expense updateExpense(Long userId, Long expenseId, String categoryName, ExpenseRequestDTO.ExpenseDTO request);

    // 유저의 지출 내역 삭제
    public abstract void deleteExpense(Long userId, Long expenseId);
}
