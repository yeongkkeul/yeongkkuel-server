package com.umc.yeongkkeul.converter;

import com.umc.yeongkkeul.apiPayload.code.status.ErrorStatus;
import com.umc.yeongkkeul.apiPayload.exception.handler.ExpenseHandler;
import com.umc.yeongkkeul.domain.Category;
import com.umc.yeongkkeul.domain.Expense;
import com.umc.yeongkkeul.domain.User;
import com.umc.yeongkkeul.repository.ExpenseRepository;
import com.umc.yeongkkeul.service.ExpenseQueryServiceImpl;
import com.umc.yeongkkeul.web.dto.CategoryRequestDTO;
import com.umc.yeongkkeul.web.dto.CategoryResponseDTO;
import com.umc.yeongkkeul.web.dto.ExpenseResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class CategoryConverter {

    // req 객체를 카테고리 객체로 변환
    public static Category toCategoryDTO(CategoryRequestDTO.CategoryDTO request, User user){
        return Category.builder()
                .name(request.getCategoryName())
                .red(request.getRed())
                .green(request.getGreen())
                .blue(request.getBlue())
                .user(user)
                .build();
    }

    // 카테고리 객체를 res 객체로 변환
    public static CategoryResponseDTO.CategoryViewDTO toCategoryViewDTO(Category category){
        return CategoryResponseDTO.CategoryViewDTO.builder()
                .categoryName(category.getName())
                .red(category.getRed())
                .green(category.getGreen())
                .blue(category.getBlue())
                .categoryId(category.getId())
                .build();
    }

    // 카테고리 리스트를 res 객체로 변환
    public static CategoryResponseDTO.CategoryViewListDTO toCategoriesViewDTO(List<Category> categoryList){
        return CategoryResponseDTO.CategoryViewListDTO.builder()

                .totalElements(categoryList.size())
                .categoryList(categoryList.stream().map(CategoryConverter::toCategoryViewDTO).collect(Collectors.toList())
                )
                .build();
    }

    // 홈 화면 - 카테고리 리스트 및 해당 유저의 지출 내역 가져오기
    public static List<CategoryResponseDTO.CategoryViewListWithHomeDTO> toCategoriesViewListWithHomeDTO(List<Category> categoryList, User user, LocalDate today) {
        return categoryList.stream()
                .map(category -> new CategoryResponseDTO.CategoryViewListWithHomeDTO(
                        category.getName(),  // 카테고리 이름만 포함
                        category.getExpenseList().stream()  // Expense 리스트를 해당 유저의 지출 내역만 가져오기
                                .filter(expense -> expense.getUser().equals(user) // 유저의 지출만!
                                         && expense.getDay().equals(today))  // 그중에서도 today 지출만!!
                                .map(expense -> new ExpenseResponseDTO.ExpenseListViewDTO(
                                        expense.getId(), expense.getContent(), expense.getAmount()))
                                .collect(Collectors.toList())
                ))
                .collect(Collectors.toList());
    }

    // 지출 화면 - 일별 사용자의 카테고리별 지출 기록(목록)조회
    public static List<CategoryResponseDTO.CategoryViewListWithExpenditureDTO> toCategoriesViewListWithExpenditureDTO(List<Category> categoryList, User user, Integer year, Integer month, Integer day) {

        LocalDate today = LocalDate.of(year, month, day);

        return categoryList.stream()
                .map(category -> new CategoryResponseDTO.CategoryViewListWithExpenditureDTO(
                        category.getName(),  // 카테고리 이름만 포함
                        category.getRed(),  // 카테고리 색상 포함
                        category.getGreen(),
                        category.getBlue(),
                        category.getExpenseList().stream()  // Expense 리스트를 해당 유저의 지출 내역만 가져오기
                                .filter(expense -> expense.getUser().equals(user) // 유저의 지출만!
                                        && expense.getDay().equals(today))  // 그중에서도 today에 해당되는 지출만!!
                                .map(expense -> new ExpenseResponseDTO.ExpenseListView2DTO(
                                        expense.getId(), expense.getContent(), expense.getAmount()))
                                .collect(Collectors.toList())
                ))
                .collect(Collectors.toList());
    }

    // 카테고리별 주간 지출액 계산
    public static Integer getCategoryWithWeeklyExpenditure(List<Expense> expenses, Long categoryId) {

        int CategoryWithWeeklyExpenditure = expenses.stream()
                .filter(expense -> expense.getCategory().getId().equals(categoryId)) // 해당 카테고리 id로 비교
                .mapToInt(Expense::getAmount)  // 지출 금액 합산
                .sum();

        return CategoryWithWeeklyExpenditure;
    }

    // 주간 지출에서 가장 많은 지출을 한 카테고리 이름 찾기
    public static String getCategoryWithHighestExpenditure(User user, List<Category> categoryList, List<Expense> expenses, LocalDate startDay, LocalDate endDay) {
        // 각 카테고리별 주간 지출액을 계산하고 가장 큰 지출액을 가진 카테고리 찾기
        CategoryResponseDTO.CategoryViewListWithWeeklyExpenditureDTO highestExpenditureCategory = categoryViewListWithWeeklyExpenditureDTO(categoryList, expenses, user, startDay, endDay).stream()
                .max(Comparator.comparingInt(CategoryResponseDTO.CategoryViewListWithWeeklyExpenditureDTO::getTotalExpenditure)) // 지출액 비교
                .orElseThrow(() -> new ExpenseHandler(ErrorStatus.EXPENSE_CATEGORY_NOT_FOUND)); // 예외 처리

        return highestExpenditureCategory.getCategoryName(); // 가장 큰 지출액을 가진 카테고리 이름 반환
    }

    // 주간 지출 조회 화면 - 해당 주간동안의 카테고리별 지출 총액 조회
    public static List<CategoryResponseDTO.CategoryViewListWithWeeklyExpenditureDTO> categoryViewListWithWeeklyExpenditureDTO(List<Category> categoryList, List<Expense> expenses, User user, LocalDate startDay, LocalDate endDay) {

        return categoryList.stream()
                .map(category -> new CategoryResponseDTO.CategoryViewListWithWeeklyExpenditureDTO(
                        category.getName(),
                        category.getRed(),
                        category.getGreen(),
                        category.getBlue(),
                        getCategoryWithWeeklyExpenditure(expenses, category.getId())
                ))
                .collect(Collectors.toList());
    }
}
