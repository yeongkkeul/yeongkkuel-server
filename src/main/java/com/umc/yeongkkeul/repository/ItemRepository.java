package com.umc.yeongkkeul.repository;

import com.umc.yeongkkeul.domain.Item;
import com.umc.yeongkkeul.domain.enums.ItemType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ItemRepository extends JpaRepository<Item, Long> {
    Optional<Item> findById(Long id);

    List<Item> findAllByTypeOrderByCreatedAt(ItemType type);


}
