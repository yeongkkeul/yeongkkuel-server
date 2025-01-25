package com.umc.yeongkkeul.web.controller;

import com.umc.yeongkkeul.apiPayload.ApiResponse;
import com.umc.yeongkkeul.domain.Expense;
import com.umc.yeongkkeul.domain.User;
import com.umc.yeongkkeul.service.ExpenseCommandService;
import com.umc.yeongkkeul.web.dto.ExpenseRequestDTO;
import com.umc.yeongkkeul.web.dto.MyPageInfoResponseDto;
import com.umc.yeongkkeul.web.dto.UserRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import static com.umc.yeongkkeul.security.FindLoginUser.getCurrentUserId;

@RestController
@RequiredArgsConstructor
@Tag(name = "지출 API", description = "지출 관련 API 입니다.")
//@RequestMapping("/api/expense")
public class ExpenseController {
    private final ExpenseCommandService expenseCommandService;

    @PostMapping("/api/expense")
    @Operation(summary = "지출 내역 생성",description = "지출 내역을 입력합니다.")
    public ApiResponse<Expense> createExpense(
            @RequestBody @Valid ExpenseRequestDTO.ExpenseDTO request
    ) {
        String userEmail = getCurrentUserId();

        String categoryName = request.getCategory();

        Expense response = expenseCommandService.createExpense(userEmail, categoryName, request);
        return ApiResponse.onSuccess(response);
    }

    @PatchMapping("/api/expense/{expenseId}")
    @Operation(summary = "지출 내역 수정",description = "지출 수정 내역을 입력합니다.")
    public ApiResponse<Expense> updateExpense(
            @PathVariable("expenseId") Long expenseId,
            @RequestBody @Valid ExpenseRequestDTO.ExpenseDTO request
    ){
        String userEmail = getCurrentUserId();

        String categoryName = request.getCategory();

        Expense response = expenseCommandService.updateExpense(userEmail, expenseId, categoryName, request);
        return ApiResponse.onSuccess(response);
    }

    @DeleteMapping("/api/expense/{expenseId}")
    @Operation(summary = "지출 내역 삭제",description = "지출 내역을 삭제합니다.")
    public ApiResponse<?> deleteExpense(
            @PathVariable("expenseId") Long expenseId
    ){
        // getCurrentUserId는 현재 사용자 이메일을 반환해줌
         String userEmail = getCurrentUserId();

        expenseCommandService.deleteExpense(userEmail, expenseId);

        return ApiResponse.onSuccess(null);
    }

    @PostMapping("/api/expenditure/target")
    @Operation(summary = "유저의 하루 목표 지출액 설정",description = "하루 목표 지출액을 설정합니다.")
    public ApiResponse<User> getUserDayTargetExpenditure(
            @RequestBody @Valid ExpenseRequestDTO.DayTargetExpenditureRequestDto request
    ){
        String userEmail = getCurrentUserId();

        User response = expenseCommandService.getDayTargetExpenditureRequest(userEmail, request);

        return ApiResponse.onSuccess(response);
    }

}
