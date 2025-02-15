package com.umc.yeongkkeul.web.controller;

import com.github.f4b6a3.tsid.TsidCreator;
import com.umc.yeongkkeul.apiPayload.ApiResponse;
import com.umc.yeongkkeul.domain.Expense;
import com.umc.yeongkkeul.domain.User;
import com.umc.yeongkkeul.domain.mapping.ChatRoomMembership;
import com.umc.yeongkkeul.repository.ChatRoomMembershipRepository;
import com.umc.yeongkkeul.service.ChatService;
import com.umc.yeongkkeul.service.ExpenseCommandService;
import com.umc.yeongkkeul.service.ExpenseQueryServiceImpl;
import com.umc.yeongkkeul.web.dto.ExpenseRequestDTO;
import com.umc.yeongkkeul.web.dto.ExpenseResponseDTO;
import com.umc.yeongkkeul.web.dto.MyPageInfoResponseDto;
import com.umc.yeongkkeul.web.dto.UserRequestDto;
import com.umc.yeongkkeul.web.dto.chat.MessageDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import com.umc.yeongkkeul.web.dto.chat.MessageDto;

import java.time.LocalDateTime;
import java.util.List;

import static com.umc.yeongkkeul.security.FindLoginUser.getCurrentUserId;
import static com.umc.yeongkkeul.security.FindLoginUser.toId;

@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(name = "지출 API", description = "지출 관련 API 입니다.")
//@RequestMapping("/api/expense")
public class ExpenseController {
    private final ExpenseCommandService expenseCommandService;
    private final ExpenseQueryServiceImpl expenseQueryServiceImpl;
    private final ChatService chatService;

    @PostMapping("/api/expense")
    @Operation(summary = "지출 내역 생성",description = "지출 내역을 입력합니다.")
    public ApiResponse<Expense> createExpense(
            @RequestBody @Valid ExpenseRequestDTO.ExpenseDTO request
    ) {
        Long userId = toId(getCurrentUserId());

        Expense response = expenseCommandService.createExpense(userId, request);

        chatService.sendReceiptChatRoom(response, userId);

        return ApiResponse.onSuccess(response);
    }

    @PatchMapping("/api/expense/{expenseId}")
    @Operation(summary = "지출 내역 수정",description = "지출 수정 내역을 입력합니다.")
    public ApiResponse<Expense> updateExpense(
            @PathVariable("expenseId") Long expenseId,
            @RequestBody @Valid ExpenseRequestDTO.ExpenseUpdateDTO request
    ){
        Long userId = toId(getCurrentUserId());

        Expense response = expenseCommandService.updateExpense(userId, expenseId, request);
        return ApiResponse.onSuccess(response);
    }

    @DeleteMapping("/api/expense/{expenseId}")
    @Operation(summary = "지출 내역 삭제",description = "지출 내역을 삭제합니다.")
    public ApiResponse<?> deleteExpense(
            @PathVariable("expenseId") Long expenseId
    ){
        Long userId = toId(getCurrentUserId());

        expenseCommandService.deleteExpense(userId, expenseId);

        return ApiResponse.onSuccess(null);
    }

    @PostMapping("/api/expenditures/target")
    @Operation(summary = "유저의 하루 목표 지출액 설정",description = "하루 목표 지출액을 설정합니다.")
    public ApiResponse<User> getUserDayTargetExpenditure(
            @RequestBody @Valid ExpenseRequestDTO.DayTargetExpenditureRequestDto request
    ){
        Long userId = toId(getCurrentUserId());

        User response = expenseCommandService.getDayTargetExpenditureRequest(userId, request);

        return ApiResponse.onSuccess(response);
    }

    @GetMapping("/api/expenditures/day")
    @Operation(summary = "일간 - 하루 목표 지출액 조회", description = "유저의 하루 목표 지출액을 조회합니다.")
    public ApiResponse<ExpenseResponseDTO.DayTargetExpenditureViewDTO> DayTargetExpenditureView(){
        Long userId = toId(getCurrentUserId());
        return ApiResponse.onSuccess(expenseQueryServiceImpl.DayTargetExpenditureViewDTO(userId));
    }

    @GetMapping("/api/expenditures/{year}/{month}/{day}")
    @Operation(summary = "카테고리별 지출 기록 조회", description = "카테고리별 지출 기록을 조회합니다.")
    public ApiResponse<ExpenseResponseDTO.CategoryListExpenditureViewDTO> CategoryExpenseListView(
            @PathVariable("year") Integer year,
            @PathVariable("month") Integer month,
            @PathVariable("day") Integer day
    ){
        Long userId = toId(getCurrentUserId());
        return ApiResponse.onSuccess(expenseQueryServiceImpl.CategoryExpenseListView(userId, year, month, day));
    }

    @GetMapping("/api/expenditures/week/expenses")
    @Operation(summary = "주간 - 지출액 조회", description = "해당 주간의 지출액을 조회합니다.")
    public ApiResponse<ExpenseResponseDTO.WeeklyExpenditureViewDTO> WeeklyExpenseListView(){
        Long userId = toId(getCurrentUserId());
        return ApiResponse.onSuccess(expenseQueryServiceImpl.getWeeklyExpenditure(userId));
    }

    @GetMapping("/api/expenditures/week/average")
    @Operation(summary = "주간 - 주간 통계 조회", description = "해당 주간의 통계를 조회합니다.")
    public ApiResponse<ExpenseResponseDTO.WeeklyAverageExpenditureViewDTO> WeeklyAverageExpenditureView(){
        Long userId = toId(getCurrentUserId());
        return ApiResponse.onSuccess(expenseQueryServiceImpl.weeklyAverageExpenditureViewDTO(userId));
    }

    @GetMapping("/api/expenditures/month/{year}/{month}")
    @Operation(summary = "월간 - 달력 조회", description = "해당 월의 지출 내역을 조회합니다.")
    public ApiResponse<ExpenseResponseDTO.MonthlyExpenditureViewDTO> MonthlyExpenditureView(
            @PathVariable("year") Integer year,
            @PathVariable("month") Integer month
    ){
        Long userId = toId(getCurrentUserId());
        return ApiResponse.onSuccess(expenseQueryServiceImpl.monthlyExpenditureViewDTO(userId, year, month));
    }
}
