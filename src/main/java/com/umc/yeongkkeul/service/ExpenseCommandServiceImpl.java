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
    public Expense createExpense(String userEmail, String categoryName, ExpenseRequestDTO.ExpenseDTO request){
        // 유저 찾기
        System.out.println("user는 "+ userEmail);
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));

        // 유저의 카테고리 찾기
        Category category = categoryRepository.findByName(categoryName)
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

        // 유저의 지출 내역 저장
        return expenseRepository.save(expense);
    }

    // 유저의 지출 내역 수정
    @Override
    public Expense updateExpense(String userEmail, Long expenseId, String categoryName, ExpenseRequestDTO.ExpenseDTO request){
        // 유저 찾기
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));

        // 유저의 카테고리 찾기
        Category category = categoryRepository.findByName(categoryName)
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
        expense.setImageUrl(request.getExpenseImg());

        Integer amount = request.getAmount();

        // is_no_spending(무지출 여부)가 true이면 지출 0원
        if (request.getIsExpense() == true) {
            amount = 0;
        }

        expense.setAmount(amount);

        return expenseRepository.save(expense);
    }

    @Override
    public void deleteExpense(String userEmail, Long expenseId){
        // 유저 찾기
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));

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
}
