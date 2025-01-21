package com.umc.yeongkkeul.service;

import com.umc.yeongkkeul.web.dto.CategoryRequestDTO;
import com.umc.yeongkkeul.web.dto.CategoryResponseDTO;

public interface CategoryQueryService {
    // 단일 조회
    CategoryResponseDTO.CategoryViewDTO viewCategory(Long categoryId, Long userId);

    // 목록 조회
    CategoryResponseDTO.CategoryViewListDTO viewCategories(Long userId);
}
