package com.umc.yeongkkeul.repository;

import com.umc.yeongkkeul.domain.common.Uuid;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UuidRepository extends JpaRepository<Uuid, Long> {
}
