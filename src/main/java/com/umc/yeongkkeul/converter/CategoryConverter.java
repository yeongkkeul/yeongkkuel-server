package com.umc.yeongkkeul.converter;

import com.umc.yeongkkeul.domain.Category;
import com.umc.yeongkkeul.domain.User;
import com.umc.yeongkkeul.web.dto.CategoryRequestDTO;
import com.umc.yeongkkeul.web.dto.CategoryResponseDTO;

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

}
