package com.umc.yeongkkeul.repository;

import com.umc.yeongkkeul.domain.Expense;
import com.umc.yeongkkeul.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long>  {
    Optional<Expense> findById(Long id);
}
