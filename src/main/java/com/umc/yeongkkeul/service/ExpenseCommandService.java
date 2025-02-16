package com.umc.yeongkkeul.service;

import com.umc.yeongkkeul.domain.Expense;
import com.umc.yeongkkeul.domain.User;
import com.umc.yeongkkeul.web.dto.ExpenseRequestDTO;
import com.umc.yeongkkeul.web.dto.MyPageInfoResponseDto;
import com.umc.yeongkkeul.web.dto.UserRequestDto;
import org.springframework.web.multipart.MultipartFile;

public abstract class ExpenseCommandService {
    // 유저의 지출 내역 생성
    public abstract Expense createExpense(Long userId, ExpenseRequestDTO.ExpenseDTO request, MultipartFile expenseImage);

    // 유저의 지출 내역 수정
    public abstract Expense updateExpense(Long userId, Long expenseId, ExpenseRequestDTO.ExpenseUpdateDTO request, MultipartFile expenseImage);

    // 유저의 지출 내역 삭제
    public abstract void deleteExpense(Long userId, Long expenseId);

    // 유저의 하루 목표 지출액 설정
    public abstract User getDayTargetExpenditureRequest(Long userId, ExpenseRequestDTO.DayTargetExpenditureRequestDto request);
}
