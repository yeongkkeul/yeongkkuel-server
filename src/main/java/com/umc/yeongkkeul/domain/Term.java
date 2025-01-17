package com.umc.yeongkkeul.domain;

import com.umc.yeongkkeul.domain.common.BaseEntity;
import com.umc.yeongkkeul.domain.enums.TermType;
import com.umc.yeongkkeul.domain.mapping.UserTerms;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Term extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "term_type", nullable = false)
    private TermType termType;

    @Column(name = "version", nullable = false)
    private int version;

    @Column(name = "title", nullable = false, length = 20)
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "is_mandatory", nullable = false)
    private boolean isMandatory;

    @OneToMany(mappedBy = "term", cascade = CascadeType.ALL)
    private List<UserTerms> userTermList = new ArrayList<>();
}
