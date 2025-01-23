package com.umc.yeongkkeul.repository;

import com.umc.yeongkkeul.domain.mapping.UserTerms;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserTermsRepository extends JpaRepository<UserTerms, Long> {

    Boolean existsByUser_Email(String email);
}
