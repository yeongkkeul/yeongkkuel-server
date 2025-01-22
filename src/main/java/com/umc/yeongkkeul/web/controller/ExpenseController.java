package com.umc.yeongkkeul.web.controller;

import com.umc.yeongkkeul.apiPayload.ApiResponse;
import com.umc.yeongkkeul.domain.Category;
import com.umc.yeongkkeul.domain.Expense;
import com.umc.yeongkkeul.service.ExpenseService;
import com.umc.yeongkkeul.web.dto.ExpenseRequestDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "지출 API", description = "지출 관련 API 입니다.")
@RequestMapping("/api/expense")
public class ExpenseController {
    private final ExpenseService expenseCommandService;

    @PostMapping("/")
    @Operation(summary = "지출 내역 생성",description = "지출 내역을 입력합니다.")
    public ApiResponse<Expense> createExpense(
//            @PathVariable Long userId,
//            @PathVariable Long categoryId,
            @RequestBody @Valid ExpenseRequestDTO.ExpenseDTO request
    ) {
        // TODO : 추후에 JWT를 통해 userID 가져오는 로직으로 변경 예정
        Long userId = request.getUserId();

//        Long categoryId = request.getCategoryId();
        String categoryName = request.getCategory();

        Expense response = expenseCommandService.createExpense(userId, categoryName, request);
        return ApiResponse.onSuccess(response);
    }

    @PatchMapping("/{expenseId}")
    @Operation(summary = "지출 내역 수정",description = "지출 수정 내역을 입력합니다.")
    public ApiResponse<Expense> updateExpense(
            @PathVariable("expenseId") Long expenseId,
//            @PathVariable Long categoryId,
            @RequestBody @Valid ExpenseRequestDTO.ExpenseDTO request
    ){
        // TODO : 추후에 JWT를 통해 userID 가져오는 로직으로 변경 예정
        Long userId = request.getUserId();

        String categoryName = request.getCategory();

        Expense response = expenseCommandService.updateExpense(userId, expenseId, categoryName, request);
        return ApiResponse.onSuccess(response);
    }

    @DeleteMapping("/{expenseId}")
    @Operation(summary = "지출 내역 삭제",description = "지출 내역을 삭제합니다.")
    public ApiResponse<?> deleteExpense(
            @PathVariable("expenseId") Long expenseId
    ){
        // TODO : 추후에 JWT를 통해 userID 가져오는 로직으로 변경 예정
        Long userId = 1L;

        expenseCommandService.deleteExpense(userId, expenseId);

        return ApiResponse.onSuccess(null);
    }

}
