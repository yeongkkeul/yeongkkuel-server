package com.umc.yeongkkeul.repository;

import com.umc.yeongkkeul.domain.Reward;
import com.umc.yeongkkeul.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RewardRepository extends JpaRepository<Reward, Long> {

    List<Reward> findByUser(User user);
}