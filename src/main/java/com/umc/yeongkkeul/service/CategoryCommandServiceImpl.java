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
        if(categoryRepository.existsByUserAndName(user ,category.getName())){
            // 해당 유저가 이미 생성한 것들 중 중복ㅇㅣ 있는지
            throw new CategoryHandler(ErrorStatus.CATEGORY_DUPLICATE);
        }
        user.addCategory(category);
        categoryRepository.save(category);


        return CategoryConverter.toCategoryViewDTO(category);
    }

    @Override
    public CategoryResponseDTO.CategoryViewDTO updateCategory(Long categoryId, CategoryRequestDTO.CategoryDTO request, Long userId) {
        // 유저 찾기, 없으면 에러
        User user = userRepository.findById(userId).orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));

        // 카테고리 존재 여부 확인
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new UserHandler(ErrorStatus.CATEGORY_NOT_FOUND));

        // 권한 검증: 카테고리 주인이 현재 유저인지
        if (!category.getUser().getId().equals(user.getId())) {
            throw new CategoryHandler(ErrorStatus.CATEGORY_NO_PERMISSION);
        }

        // 새로운 값으로 업데이트
        category.setName(request.getCategoryName());
        category.setRed(request.getRed());
        category.setGreen(request.getGreen());
        category.setBlue(request.getBlue());

        // "자신"을 제외한 중복 검사 - 내 카테고리들 중 이름이 같고, id가 다른 경우가 존재하는지? -> 중복이라 변경X
        if (categoryRepository.existsByUserAndNameAndIdNot(user, category.getName(), categoryId)) {
            throw new CategoryHandler(ErrorStatus.CATEGORY_DUPLICATE);
        }

        Category updated = categoryRepository.save(category);
        return CategoryConverter.toCategoryViewDTO(updated);
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
        user.removeCategory(category);
        categoryRepository.deleteById(category.getId());
    }
}
