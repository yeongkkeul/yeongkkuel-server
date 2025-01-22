package com.umc.yeongkkeul.repository;

import com.umc.yeongkkeul.domain.Term;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TermRepository extends JpaRepository<Term, Long> {

    Term findTermById(Long id);
}
