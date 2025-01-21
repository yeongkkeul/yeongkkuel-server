package com.umc.yeongkkeul.web.controller;

import com.umc.yeongkkeul.apiPayload.ApiResponse;
import com.umc.yeongkkeul.service.CategoryCommandService;
import com.umc.yeongkkeul.service.CategoryQueryService;
import com.umc.yeongkkeul.web.dto.CategoryRequestDTO;
import com.umc.yeongkkeul.web.dto.CategoryResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/category")
@RequiredArgsConstructor
@Validated
public class CategoryController {

    private final CategoryCommandService categoryCommandService;
    private final CategoryQueryService categoryQueryService;

    @PostMapping
    public ApiResponse<CategoryResponseDTO.CategoryViewDTO> createCategory(
            @RequestBody @Valid CategoryRequestDTO.CategoryDTO request
    ) {
        // JWT가 없으므로, 임시로 하드코딩된 userId 사용
        // TODO : JWT를 통해 userID 가져오는 로직으로 변경
        Long userId = 1L;

        CategoryResponseDTO.CategoryViewDTO response = categoryCommandService.addCategory(request, userId);
        return ApiResponse.onSuccess(response);
    }

    @PatchMapping("/{categoryId}")
    public ApiResponse<CategoryResponseDTO.CategoryViewDTO> updateCategory(
            @PathVariable Long categoryId,
            @RequestBody @Valid CategoryRequestDTO.CategoryDTO request
    ) {
        // JWT가 없으므로, 임시로 하드코딩된 userId 사용
        // TODO : JWT를 통해 userID 가져오는 로직으로 변경
        Long userId = 1L;

        CategoryResponseDTO.CategoryViewDTO response = categoryCommandService.updateCategory(categoryId, request, userId);
        return ApiResponse.onSuccess(response);
    }


    @GetMapping("/{categoryId}")
    public ApiResponse<CategoryResponseDTO.CategoryViewDTO> viewCategory(
            @PathVariable Long categoryId
    ){
        // JWT가 없으므로, 임시로 하드코딩된 userId 사용
        // TODO : JWT를 통해 userID 가져오는 로직으로 변경
        Long userId = 1L;

        CategoryResponseDTO.CategoryViewDTO response = categoryQueryService.viewCategory(categoryId, userId);
        return ApiResponse.onSuccess(response);
    }

    @GetMapping("/categories")
    public ApiResponse<CategoryResponseDTO.CategoryViewListDTO> viewCategories(){
        // JWT가 없으므로, 임시로 하드코딩된 userId 사용
        // TODO : JWT를 통해 userID 가져오는 로직으로 변경
        Long userId = 1L;

        CategoryResponseDTO.CategoryViewListDTO response = categoryQueryService.viewCategories(userId);
        return ApiResponse.onSuccess(response);
    }


    @DeleteMapping("/{categoryId}")
    public ApiResponse<?> deleteCategory(@PathVariable Long categoryId) {
        // JWT가 없으므로, 임시로 하드코딩된 userId 사용
        // TODO : JWT를 통해 userID 가져오는 로직으로 변경
        Long userId = 1L;

        categoryCommandService.deleteCategory(categoryId, userId);

        // 원하는 형태의 응답을 직접 생성
        return ApiResponse.builder()
                .isSuccess(true)
                .code("2000")
                .message("카테고리가 성공적으로 삭제되었습니다.")
                .build();
    }
}
