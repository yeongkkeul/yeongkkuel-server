package com.umc.yeongkkeul.repository;

import com.umc.yeongkkeul.domain.Reward;
import com.umc.yeongkkeul.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RewardRepository extends JpaRepository<Reward, Long> {

    List<Reward> findByUser(User user);

    @Query("SELECT r FROM Reward r WHERE r.user.id = :userId AND YEAR(r.createdAt) = :year AND MONTH(r.createdAt) = :month")
    List<Reward> findByUserIdAndYearAndMonth(@Param("userId") Long userId, @Param("year") int year, @Param("month") int month);
}