package com.umc.yeongkkeul.service;

import com.umc.yeongkkeul.domain.Expense;
import com.umc.yeongkkeul.web.dto.CategoryResponseDTO;
import com.umc.yeongkkeul.web.dto.ExpenseResponseDTO;

public interface ExpenseQueryService {
    ExpenseResponseDTO.DayTargetExpenditureViewDTO DayTargetExpenditureViewDTO(Long userId);

    ExpenseResponseDTO.CategoryListExpenditureViewDTO CategoryExpenseListView(Long userId, Integer year, Integer month, Integer day);

    ExpenseResponseDTO.WeeklyExpenditureViewDTO getWeeklyExpenditure(Long userId);

    ExpenseResponseDTO.WeeklyAverageExpenditureViewDTO weeklyAverageExpenditureViewDTO(Long userId);
}
