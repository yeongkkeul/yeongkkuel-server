package com.umc.yeongkkeul.service;

import com.umc.yeongkkeul.apiPayload.code.status.ErrorStatus;
import com.umc.yeongkkeul.apiPayload.exception.handler.ExpenseHandler;
import com.umc.yeongkkeul.apiPayload.exception.handler.UserHandler;
import com.umc.yeongkkeul.converter.CategoryConverter;
import com.umc.yeongkkeul.domain.Category;
import com.umc.yeongkkeul.domain.Expense;
import com.umc.yeongkkeul.domain.User;
import com.umc.yeongkkeul.repository.CategoryRepository;
import com.umc.yeongkkeul.repository.ExpenseRepository;
import com.umc.yeongkkeul.repository.UserRepository;
import com.umc.yeongkkeul.web.dto.CategoryResponseDTO;
import com.umc.yeongkkeul.web.dto.ExpenseResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ExpenseQueryServiceImpl implements ExpenseQueryService {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ExpenseRepository expenseRepository;

    // 일간 - 유저의 하루 목쵸 지출액 조회
    @Override
    public ExpenseResponseDTO.DayTargetExpenditureViewDTO DayTargetExpenditureViewDTO(String userEmail) {
        // 유저 찾기
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));

        // 만약 유저가 하루 목표 지출액을 설정해둔 적이 없다면 에러
        if (user.getDayTargetExpenditure() == null) {
            throw new ExpenseHandler(ErrorStatus.EXPENSE_DAY_TARGET_EXPENDITURE_NOT_FOUND);
        }

        return new ExpenseResponseDTO.DayTargetExpenditureViewDTO().builder()
                .dayTargetExpenditure(user.getDayTargetExpenditure())
                .build();
    }

    // 일간 - 카테고리별 지출 기록(목록) 조회
    @Override
    public ExpenseResponseDTO.CategoryListExpenditureViewDTO CategoryExpenseListView(String userEmail, Integer year, Integer month, Integer day){
        // 유저 찾기
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));

        // 카테고리 및 지출 정보 가져오기 (이때 year, month, day 넘기기)
        List<Category> categories = categoryRepository.findAllByUserId(user.getId());
        List<CategoryResponseDTO.CategoryViewListWithExpenditureDTO> categoryList
                = CategoryConverter.toCategoriesViewListWithExpenditureDTO(categories, user, year, month, day);

        return ExpenseResponseDTO.CategoryListExpenditureViewDTO.builder()
                .categories(categoryList)
                .build();
    }

    // 주간 - 총 지출액 조회
    public ExpenseResponseDTO.WeeklyExpenditureViewDTO getWeeklyExpenditure(String userEmail) {
        // 유저 찾기
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));

        // 오늘 날짜 구하기
        LocalDate today = LocalDate.now();

        // 이번 주 월요일 구하기
        LocalDate startDay = today.with(DayOfWeek.MONDAY);
        // 이번 주 일요일 구하기
        LocalDate endDay = today.with(DayOfWeek.SUNDAY);

        // 일주일 총 지출액 계산
        Integer weekExpenditure = 0;

        // 해당 유저의 지출 내역을 월요일부터 오늘까지 필터링해서 가져오기
        List<Expense> expenses = expenseRepository.findByUserIdAndExpenseDayAtBetween(user.getId(), startDay, today);

        // 월요일부터 오늘까지의 각 요일별 지출 금액 계산
        List<ExpenseResponseDTO.ExpensePerDayDTO> expensesPerDay = new ArrayList<>();

        // 각 요일별로 지출액을 계산
        for (DayOfWeek dayOfWeek : DayOfWeek.values()) {
            // 요일 구하기
            LocalDate currentDay = startDay.with(DayOfWeek.of(dayOfWeek.getValue()));

            // 요일이 오늘 날짜 이후라면 지출액을 0으로 설정
            if (currentDay.isAfter(today)) {
                expensesPerDay.add(new ExpenseResponseDTO.ExpensePerDayDTO(
                        currentDay.format(DateTimeFormatter.ofPattern("yyyy-MM-dd, EEEE")), // "2025-01-11, Saturday"
                        0 // 아직 도래하지 않은 요일은 0으로 설정
                ));
                continue; // 해당 요일은 더 이상 계산하지 않고 넘으
            }

            // 해당 요일에 해당하는 유저의 지출 금액 구하기
            int dailyExpenditure = expenses.stream()
                    .filter(expense -> expense.getUser().getId().equals(user.getId()) && expense.getDay().equals(currentDay)) // 유저와 날짜 필터링
                    .mapToInt(Expense::getAmount) // 지출 금액 합산
                    .sum();


            // 해당 요일의 지출 내역을 리스트에 추가
            expensesPerDay.add(new ExpenseResponseDTO.ExpensePerDayDTO(
                    currentDay.format(DateTimeFormatter.ofPattern("yyyy-MM-dd, EEEE")), // "2025-01-11, Saturday"
                    dailyExpenditure
            ));
            System.out.println(" 날짜 : "+currentDay.format(DateTimeFormatter.ofPattern("yyyy-MM-dd, EEEE")));
            System.out.println((" 총 지출액 : "+ dailyExpenditure));

            // 주간 지출액 계산
            weekExpenditure += dailyExpenditure;
        }

        // 하루 목표 지출액
        Integer dayTargetExpenditure = user.getDayTargetExpenditure() != null ? user.getDayTargetExpenditure() : 0;

        // 응답 반환
        return ExpenseResponseDTO.WeeklyExpenditureViewDTO.builder()
                .weekExpenditure(weekExpenditure)
                .dayTargetExpenditure(dayTargetExpenditure)
                .expenses(expensesPerDay)
                .build();
    }
}
