package com.umc.yeongkkeul.converter;

import com.umc.yeongkkeul.domain.Category;
import com.umc.yeongkkeul.domain.User;
import com.umc.yeongkkeul.web.dto.CategoryRequestDTO;
import com.umc.yeongkkeul.web.dto.CategoryResponseDTO;
import com.umc.yeongkkeul.web.dto.ExpenseResponseDTO;

import java.time.LocalDate;
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
                                        expense.getId(), expense.getAmount()))
                                .collect(Collectors.toList())
                ))
                .collect(Collectors.toList());
    }
}
