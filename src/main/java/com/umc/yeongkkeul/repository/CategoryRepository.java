package com.umc.yeongkkeul.repository;

import com.umc.yeongkkeul.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    boolean existsByName(String name);
}
