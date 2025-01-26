package com.umc.yeongkkeul.service;

import com.umc.yeongkkeul.domain.Expense;
import com.umc.yeongkkeul.web.dto.CategoryResponseDTO;
import com.umc.yeongkkeul.web.dto.ExpenseResponseDTO;

public interface ExpenseQueryService {
    ExpenseResponseDTO.DayTargetExpenditureViewDTO DayTargetExpenditureViewDTO(String userEmail);

    ExpenseResponseDTO.CategoryListExpenditureViewDTO CategoryExpenseListView(String userEmail, Integer year, Integer month, Integer day);
}
