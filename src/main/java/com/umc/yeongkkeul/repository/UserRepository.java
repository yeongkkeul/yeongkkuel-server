package com.umc.yeongkkeul.repository;

import com.umc.yeongkkeul.domain.User;
import com.umc.yeongkkeul.domain.enums.AgeGroup;
import com.umc.yeongkkeul.domain.enums.Job;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<User> findByReferralCode(String referralCode);

    Boolean existsByOauthTypeAndEmail(String oauthType, String email);

    List<User> findByAgeGroup(AgeGroup ageGroup);

    List<User> findByJob(Job job);

    List<User> findByAgeGroupAndJob(AgeGroup ageGroup, Job job);
}
