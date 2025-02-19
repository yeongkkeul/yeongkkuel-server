package com.umc.yeongkkeul.service;

import com.umc.yeongkkeul.apiPayload.code.status.ErrorStatus;
import com.umc.yeongkkeul.apiPayload.exception.handler.CategoryHandler;
import com.umc.yeongkkeul.apiPayload.exception.handler.ExpenseHandler;
import com.umc.yeongkkeul.apiPayload.exception.handler.UserHandler;
import com.umc.yeongkkeul.aws.s3.AmazonS3Manager;
import com.umc.yeongkkeul.converter.ExpenseConverter;
import com.umc.yeongkkeul.domain.Category;
import com.umc.yeongkkeul.domain.Expense;
import com.umc.yeongkkeul.domain.User;
import com.umc.yeongkkeul.domain.common.Uuid;
import com.umc.yeongkkeul.repository.CategoryRepository;
import com.umc.yeongkkeul.repository.ExpenseRepository;
import com.umc.yeongkkeul.repository.UserRepository;
import com.umc.yeongkkeul.repository.UuidRepository;
import com.umc.yeongkkeul.web.dto.ExpenseRequestDTO;
import com.umc.yeongkkeul.web.dto.MyPageInfoResponseDto;
import com.umc.yeongkkeul.web.dto.NotificationDetailRequestDto;
import com.umc.yeongkkeul.web.dto.UserRequestDto;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class ExpenseCommandServiceImpl extends ExpenseCommandService {

    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final UuidRepository uuidRepository;
    private final AmazonS3Manager amazonS3Manager;
    private final NotificationService notificationService;

    // 유저의 지출 내역 생성
    @Override
    public Expense createExpense(Long userId, ExpenseRequestDTO.ExpenseDTO request, MultipartFile expenseImage){
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

        String expenseImageUrl = null;
        if (expenseImage != null && !expenseImage.isEmpty()) {
            Uuid uuidEntity = Uuid.builder().uuid(UUID.randomUUID().toString()).build();
            uuidRepository.save(uuidEntity);

            String keyName = amazonS3Manager.generateExpenseImageKeyName(uuidEntity);

            expenseImageUrl = amazonS3Manager.uploadFile(keyName, expenseImage);
        }

        Expense expense = ExpenseConverter.createExpense(request, user, category, request.getIsExpense());
        expense.setImageUrl(expenseImageUrl);  // 이미지 URL 추가

        category.getExpenseList().add(expense);

        updateConsecutiveNoSpendingDays(expense);

        // 유저의 지출 내역 저장
        return expenseRepository.save(expense);
    }

    // 유저의 지출 내역 수정
    @Override
    public Expense updateExpense(Long userId, Long expenseId, ExpenseRequestDTO.ExpenseUpdateDTO request, MultipartFile expenseImage){
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

        // 해당 지출이 이 유저의 지출 내역이 맞는지
        if(!expense.getUser().getId().equals(user.getId())){
            // 본인의 지출이 아니므로 권한 에러
            throw new ExpenseHandler(ErrorStatus.EXPENSE_NOT_FOUND);
        }

        // 지출액이 음수일 경우 에러
        if (request.getAmount() < 0){
            throw new ExpenseHandler(ErrorStatus.EXPENSE_AMOUNT_ERROR);
        }

        String expenseImageUrl = expense.getImageUrl();

        if (expenseImage != null && !expenseImage.isEmpty()) {
            Uuid uuidEntity = Uuid.builder().uuid(UUID.randomUUID().toString()).build();
            uuidRepository.save(uuidEntity);

            String keyName = amazonS3Manager.generateExpenseImageKeyName(uuidEntity);

            expenseImageUrl = amazonS3Manager.uploadFile(keyName, expenseImage);
        }

        // 새로운 값으로 업데이트
        expense.setDay(request.getDay());
        expense.setCategory(category);
        expense.setContent(request.getContent());
        expense.setAmount(request.getAmount());
        expense.setImageUrl(expenseImageUrl);

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

    /**
     * 무지출 일수 갱신 로직 트랜잭션
     */
    @Transactional
    public void updateConsecutiveNoSpendingDays(Expense expense) {
        Category category = expense.getCategory();
        User user = expense.getUser();

        if (expense.getIsNoSpending()) {
            LocalDate currentDay = expense.getDay();
            LocalDate yesterday = currentDay.minusDays(1);

            // 어제 같은 카테고리에 '무지출'이 있었는지 조회
            boolean hasNoSpendingYesterday = expenseRepository.existsNoSpendingExpense(
                    user.getId(),
                    category.getId(),
                    yesterday
            );

            if (hasNoSpendingYesterday) {
                // 연속 일수 +1
                category.setConsecutiveNoSpendingDays(category.getConsecutiveNoSpendingDays() + 1);
            } else {
                // 새롭게 시작(연속 1일)
                category.setConsecutiveNoSpendingDays(1);
            }

            // 5의 배수인지 체크
            int consecutiveDays = category.getConsecutiveNoSpendingDays();
            if (consecutiveDays % 5 == 0) {
                // (5×k) 일 연속 달성
                int k = consecutiveDays / 5;

                // 보상 로직: 10×k
                int reward = 10 * k;
                user.setRewardBalance(user.getRewardBalance() + reward);
                userRepository.save(user);

                // 알림
                notificationService.createNotification(
                        user.getId(),
                        new NotificationDetailRequestDto(
                                "AWARD_NO_SPENDING_REWARDS",
                                "[무지출 보상] " + category.getName() + " 카테고리 "
                                        + consecutiveDays + "일 연속 무지출 달성으로 "
                                        + reward + "P 획득!",
                                null
                        )
                );
            }
        } else {
            // 무지출이 아닐 경우 연속 일수 끊김
            category.setConsecutiveNoSpendingDays(0);
        }
    }

}
