package com.umc.yeongkkeul.service;

import com.umc.yeongkkeul.web.dto.CategoryRequestDTO;
import com.umc.yeongkkeul.web.dto.CategoryResponseDTO;

public interface CategoryCommandService {
    // 서비스 단에서 Converter를 호출해서 카테고리 객체로 만들고, 사용함
    // 추가 - req dto 받아서, DB에 추가하고 view 리턴
    CategoryResponseDTO.CategoryViewDTO addCategory(CategoryRequestDTO.CategoryDTO request, Long userId);

    // 삭제 - view 리턴
    void deleteCategory(Long categoryId, Long userId);
    // 수정
    CategoryResponseDTO.CategoryViewDTO updateCategory(Long categoryId, CategoryRequestDTO.CategoryDTO request, Long userId);

}
