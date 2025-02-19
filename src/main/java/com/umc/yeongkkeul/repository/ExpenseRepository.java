package com.umc.yeongkkeul.repository;

import com.umc.yeongkkeul.domain.Expense;
import com.umc.yeongkkeul.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    @Query("SELECT e FROM Expense e WHERE e.user.id = :userId AND DATE(e.createdAt) BETWEEN :startDate AND :endDate")
    List<Expense> findByUserIdAndCreatedAtBetween(@Param("userId") Long userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    Optional<Expense> findById(Long id);

    @Query("SELECT e FROM Expense e WHERE e.user.id = :userId AND DATE(e.day) BETWEEN :startDate AND :endDate")
    List<Expense> findByUserIdAndExpenseDayAtBetween(@Param("userId") Long userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    boolean existsByUserAndDay(User user, LocalDate currentDay);

    @Query("SELECT e FROM Expense e WHERE e.user.id = :userId AND e.day = :yesterday")
    List<Expense> findYesterdayExpenseByUserId(@Param("userId") Long userId, @Param("yesterday") LocalDate yesterday);

    @Query("""
        SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END
        FROM Expense e
        WHERE e.user.id = :userId
          AND e.category.id = :categoryId
          AND e.day = :day
          AND e.isNoSpending = true
    """)
    boolean existsNoSpendingExpense(
            @Param("userId") Long userId,
            @Param("categoryId") Long categoryId,
            @Param("day") LocalDate day
    );

    // 특정 유저가 특정 날짜에 가지고 있는 지출(무지출 포함) 레코드 개수를 반환
    long countByUserAndDay(User user, LocalDate day);

    // 오늘 하루에 해당되는 지출
    @Query("SELECT e FROM Expense e WHERE e.user.id = :userId AND e.day = :today")
    List<Expense> findByUserIdAndExpenseDayToday(@Param("userId") Long userId, @Param("today") LocalDate today);
}
