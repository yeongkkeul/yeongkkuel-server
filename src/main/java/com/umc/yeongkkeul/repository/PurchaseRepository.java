package com.umc.yeongkkeul.repository;

import com.umc.yeongkkeul.domain.User;
import com.umc.yeongkkeul.domain.mapping.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PurchaseRepository extends JpaRepository<Purchase, Long> {
    List<Purchase> findByUser(User user);

    List<Purchase> findAllByIsUsedAndUser_Email(Boolean isUsed, String email);

    Boolean existsByUserAndItem_Id(User user, Long itemId);

    Optional<Purchase> findByIdAndUser(Long id, User user);
}
