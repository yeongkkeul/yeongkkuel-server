package com.umc.yeongkkeul.web.controller;

import com.umc.yeongkkeul.apiPayload.ApiResponse;
import com.umc.yeongkkeul.service.CategoryCommandService;
import com.umc.yeongkkeul.service.CategoryQueryService;
import com.umc.yeongkkeul.web.dto.CategoryRequestDTO;
import com.umc.yeongkkeul.web.dto.CategoryResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/category")
@Tag(name = "카테고리 API", description = "카테고리 관련 API 입니다.")
@RequiredArgsConstructor
@Validated
public class CategoryController {

    private final CategoryCommandService categoryCommandService;
    private final CategoryQueryService categoryQueryService;

    @PostMapping
    @Operation(summary = "카테고리 생성", description = "컬러값과 이름을 받아서, 카테고리 생성합니다.")
    public ApiResponse<CategoryResponseDTO.CategoryViewDTO> createCategory(
            @RequestBody @Valid CategoryRequestDTO.CategoryDTO request,
            Principal principal
    ) {
        Long userId = Long.parseLong(principal.getName());

        CategoryResponseDTO.CategoryViewDTO response = categoryCommandService.addCategory(request, userId);
        return ApiResponse.onSuccess(response);
    }

    @PatchMapping("/{categoryId}")
    @Operation(summary = "카테고리 수정", description = "컬러값과 이름을 받아서, 카테고리 수정합니다.")
    public ApiResponse<CategoryResponseDTO.CategoryViewDTO> updateCategory(
            @PathVariable Long categoryId,
            @RequestBody @Valid CategoryRequestDTO.CategoryDTO request,
            Principal principal
    ) {
        Long userId = Long.parseLong(principal.getName());

        CategoryResponseDTO.CategoryViewDTO response = categoryCommandService.updateCategory(categoryId, request, userId);
        return ApiResponse.onSuccess(response);
    }


    @GetMapping("/{categoryId}")
    @Operation(summary = "카테고리 조회", description = "카테고리 id기반 세부 조회합니다.")
    public ApiResponse<CategoryResponseDTO.CategoryViewDTO> viewCategory(
            @PathVariable Long categoryId,
            Principal principal
    ){
        Long userId = Long.parseLong(principal.getName());

        CategoryResponseDTO.CategoryViewDTO response = categoryQueryService.viewCategory(categoryId, userId);
        return ApiResponse.onSuccess(response);
    }

    @GetMapping("/categories")
    @Operation(summary = "카테고리 목록조회", description = "카테고리 목록 조회합니다.")
    public ApiResponse<CategoryResponseDTO.CategoryViewListDTO> viewCategories(Principal principal){
        Long userId = Long.parseLong(principal.getName());

        CategoryResponseDTO.CategoryViewListDTO response = categoryQueryService.viewCategories(userId);
        return ApiResponse.onSuccess(response);
    }


    @DeleteMapping("/{categoryId}")
    @Operation(summary = "카테고리 삭제", description = "카테고리 id로 삭제")
    public ApiResponse<?> deleteCategory(@PathVariable Long categoryId, Principal principal) {
        Long userId = Long.parseLong(principal.getName());

        categoryCommandService.deleteCategory(categoryId, userId);

        // 원하는 형태의 응답을 직접 생성
        return ApiResponse.builder()
                .isSuccess(true)
                .code("2000")
                .message("카테고리가 성공적으로 삭제되었습니다.")
                .build();
    }
}
