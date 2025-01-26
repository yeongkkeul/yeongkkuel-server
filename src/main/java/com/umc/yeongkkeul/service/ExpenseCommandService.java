package com.umc.yeongkkeul.service;

import com.umc.yeongkkeul.domain.Expense;
import com.umc.yeongkkeul.domain.User;
import com.umc.yeongkkeul.web.dto.ExpenseRequestDTO;
import com.umc.yeongkkeul.web.dto.MyPageInfoResponseDto;
import com.umc.yeongkkeul.web.dto.UserRequestDto;

public abstract class ExpenseCommandService {
    // 유저의 지출 내역 생성
    public abstract Expense createExpense(String userEmail, String categoryName, ExpenseRequestDTO.ExpenseDTO request);

    // 유저의 지출 내역 수정
    public abstract Expense updateExpense(String userEmail, Long expenseId, String categoryName, ExpenseRequestDTO.ExpenseDTO request);

    // 유저의 지출 내역 삭제
    public abstract void deleteExpense(String userEmail, Long expenseId);

    // 유저의 하루 목표 지출액 설정
    public abstract User getDayTargetExpenditureRequest(String userEmail, ExpenseRequestDTO.DayTargetExpenditureRequestDto request);


    // 유저의 하루 목표 지출액 추천 - 월 평균 지출 조회
}
