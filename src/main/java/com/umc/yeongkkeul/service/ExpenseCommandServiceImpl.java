package com.umc.yeongkkeul.service;

import com.umc.yeongkkeul.apiPayload.code.status.ErrorStatus;
import com.umc.yeongkkeul.apiPayload.exception.handler.CategoryHandler;
import com.umc.yeongkkeul.apiPayload.exception.handler.ExpenseHandler;
import com.umc.yeongkkeul.apiPayload.exception.handler.UserHandler;
import com.umc.yeongkkeul.converter.ExpenseConverter;
import com.umc.yeongkkeul.domain.Category;
import com.umc.yeongkkeul.domain.Expense;
import com.umc.yeongkkeul.domain.User;
import com.umc.yeongkkeul.repository.CategoryRepository;
import com.umc.yeongkkeul.repository.ExpenseRepository;
import com.umc.yeongkkeul.repository.UserRepository;
import com.umc.yeongkkeul.web.dto.ExpenseRequestDTO;
import com.umc.yeongkkeul.web.dto.MyPageInfoResponseDto;
import com.umc.yeongkkeul.web.dto.UserRequestDto;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class ExpenseCommandServiceImpl extends ExpenseCommandService {

    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    // 유저의 지출 내역 생성
    @Override
    public Expense createExpense(Long userId, ExpenseRequestDTO.ExpenseDTO request){
        // 유저 찾기
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));

        // 유저의 카테고리 찾기
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ExpenseHandler(ErrorStatus.EXPENSE_CATEGORY_NOT_FOUND));

        // 권한 검증: 카테고리 주인이 현재 유저인지
        if (!category.getUser().getId().equals(user.getId())) {
            throw new CategoryHandler(ErrorStatus.CATEGORY_NO_PERMISSION);
        }

        // 지출액이 음수일 경우 에러
        if (request.getAmount() < 0){
            throw new ExpenseHandler(ErrorStatus.EXPENSE_AMOUNT_ERROR);
        }

        Expense expense = ExpenseConverter.createExpense(request, user, category, request.getIsExpense());

        // 양방향 연관관계의 일관성을 위해, 생성된 Expense를 Category의 expenseList에 추가
        // 이렇게 설정해야 나중에 카테고리에서 get을 통해 지출을 가져올 수 있음.
        category.getExpenseList().add(expense);

        // 유저의 지출 내역 저장
        return expenseRepository.save(expense);
    }

    // 유저의 지출 내역 수정
    @Override
    public Expense updateExpense(Long userId, Long expenseId, ExpenseRequestDTO.ExpenseUpdateDTO request){
        // 유저 찾기
        User user = userRepository.findById(userId).orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));

        // 유저의 카테고리 찾기
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ExpenseHandler(ErrorStatus.EXPENSE_CATEGORY_NOT_FOUND));

        // 권한 검증: 카테고리 주인이 현재 유저인지
        if (!category.getUser().getId().equals(user.getId())) {
            throw new CategoryHandler(ErrorStatus.CATEGORY_NO_PERMISSION);
        }

        // 유저의 지출 내역 기록 찾기 (존재하는지)
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new ExpenseHandler(ErrorStatus.EXPENSE_NOT_FOUND));

        // 지출액이 음수일 경우 에러
        if (request.getAmount() < 0){
            throw new ExpenseHandler(ErrorStatus.EXPENSE_AMOUNT_ERROR);
        }

        // 새로운 값으로 업데이트
        expense.setDay(request.getDay());
        expense.setCategory(category);
        expense.setContent(request.getContent());
        expense.setAmount(request.getAmount());
        expense.setImageUrl(request.getExpenseImg());

        return expenseRepository.save(expense);
    }

    @Override
    public void deleteExpense(Long userId, Long expenseId){
        // 유저 찾기
        User user = userRepository.findById(userId).orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));

        // 유저의 카테고리 찾기
        // Category category = categoryRepository.findByName(categoryName)
        //         .orElseThrow(() -> new ExpenseHandler(ErrorStatus.EXPENSE_CATEGORY_NOT_FOUND));

        // 유저의 지출 내역 기록 찾기 (존재하는지)
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new ExpenseHandler(ErrorStatus.EXPENSE_NOT_FOUND));

        // 해당 지출이 이 유저의 지출 내역이 맞는지
        if(!expense.getUser().getId().equals(user.getId())){
            // 본인의 지출이 아니므로 권한 에러
            throw new ExpenseHandler(ErrorStatus.EXPENSE_NOT_FOUND);
        }

        user.removeExpense(expense);
        expenseRepository.deleteById(expense.getId());
    }

    // 유저의 하루 목표 지출액 설정
    public User getDayTargetExpenditureRequest(Long userId, ExpenseRequestDTO.DayTargetExpenditureRequestDto request){
        // 유저 찾기
        User user = userRepository.findById(userId).orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));

        // 하루 목표 지출액이 음수일 경우 에러
        if (request.getDayTargetExpenditure() < 0){
            throw new ExpenseHandler(ErrorStatus.EXPENSE_DAY_TARGET_EXPENDITURE_ERROR);
        }

        // 유저의 하루 목표 지출액 설정
        user.setDayTargetExpenditure(request.getDayTargetExpenditure());

        return userRepository.save(user);
    }

}
