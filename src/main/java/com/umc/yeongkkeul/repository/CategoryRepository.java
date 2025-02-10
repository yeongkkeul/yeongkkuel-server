package com.umc.yeongkkeul.repository;

import com.umc.yeongkkeul.domain.Category;
import com.umc.yeongkkeul.domain.User;
import com.umc.yeongkkeul.domain.mapping.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    // 유저와 이름 둘 다 만족하는 row가 있는지 확인
    boolean existsByUserAndName(User user, String name);
    boolean existsByUserAndNameAndIdNot(User user, String name, Long categoryId);

    List<Category> findAllByUserId(Long userId);

    Optional<Category> findById(Long id);

    // 유저와 이름으로 카테고리 찾기
    Optional<Category> findByUserAndName(User user, String name);
}
