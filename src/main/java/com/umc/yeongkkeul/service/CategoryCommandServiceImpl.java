package com.umc.yeongkkeul.service;

import com.umc.yeongkkeul.apiPayload.code.status.ErrorStatus;
import com.umc.yeongkkeul.apiPayload.exception.handler.CategoryHandler;
import com.umc.yeongkkeul.apiPayload.exception.handler.UserHandler;
import com.umc.yeongkkeul.converter.CategoryConverter;
import com.umc.yeongkkeul.domain.Category;
import com.umc.yeongkkeul.domain.User;
import com.umc.yeongkkeul.repository.CategoryRepository;
import com.umc.yeongkkeul.repository.UserRepository;
import com.umc.yeongkkeul.web.dto.CategoryRequestDTO;
import com.umc.yeongkkeul.web.dto.CategoryResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class CategoryCommandServiceImpl implements CategoryCommandService{

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    @Override
    public CategoryResponseDTO.CategoryViewDTO addCategory(CategoryRequestDTO.CategoryDTO request, Long userId) {
        // 유저 찾기, 없으면 에러
        User user = userRepository.findById(userId).orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));
        
        // 요청정보를 바탕으로 카테고리 생성
        Category category = CategoryConverter.toCategoryDTO(request, user);
        if(categoryRepository.existsByName(category.getName())){
            throw new CategoryHandler(ErrorStatus.CATEGORY_DUPLICATE);
        }
        categoryRepository.save(category);


        return CategoryConverter.toCategoryViewDTO(category);
    }

    @Override
    public void deleteCategory(Long categoryId, Long userId) {
        // 유저 찾기
        User user = userRepository.findById(userId).orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));

        // 카테고리 찾고, 없으면 에러 던지기
        Category category = categoryRepository.findById(categoryId).orElseThrow(() -> new CategoryHandler(ErrorStatus.CATEGORY_NOT_FOUND));

        // 해당 카테고리가 이 유저의 것인지 확인 (권한 검증)
        if (!category.getUser().getId().equals(user.getId())) {
            // 본인의 카테고리가 아니므로 권한 에러
            throw new CategoryHandler(ErrorStatus.CATEGORY_NO_PERMISSION);
        }

        categoryRepository.deleteById(category.getId());
    }
}
