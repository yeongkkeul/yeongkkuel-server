package com.umc.yeongkkeul.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

// 카테고리 객체 -> res
public class CategoryResponseDTO {
    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryViewDTO{
        Long categoryId;
        String categoryName;
        int red;
        int green;
        int blue;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryViewListDTO{
        List<CategoryViewDTO> categoryList;
        int totalElements;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryViewListWithHomeDTO{
        String categoryName;
        List<ExpenseResponseDTO.ExpenseListViewDTO> expenses;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryViewListWithExpenditureDTO{
        String categoryName;
        int red;
        int green;
        int blue;
        List<ExpenseResponseDTO.ExpenseListView2DTO> expenses;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryViewListWithWeeklyExpenditureDTO{
        String categoryName;
        int red;
        int green;
        int blue;
        Integer totalExpenditure;
    }
}
