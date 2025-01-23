package com.umc.yeongkkeul.repository;

import com.umc.yeongkkeul.domain.Category;
import com.umc.yeongkkeul.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    // 유저와 이름 둘 다 만족하는 row가 있는지 확인
    boolean existsByUserAndName(User user, String name);
    boolean existsByUserAndNameAndIdNot(User user, String name, Long categoryId);

    List<Category> findAllByUserId(Long userId);
}
