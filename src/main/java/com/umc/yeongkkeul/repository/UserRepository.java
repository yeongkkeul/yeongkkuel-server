package com.umc.yeongkkeul.repository;

import com.umc.yeongkkeul.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);

}
