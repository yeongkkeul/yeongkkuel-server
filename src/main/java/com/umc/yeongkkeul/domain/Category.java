package com.umc.yeongkkeul.domain;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.umc.yeongkkeul.domain.common.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Category extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 16)
    private String name;

    @Min(value = 0)
    @Max(value = 255)
    @Column(name = "red", nullable = false)
    private int red;

    @Min(value = 0)
    @Max(value = 255)
    @Column(name = "green", nullable = false)
    private int green;

    @Min(value = 0)
    @Max(value = 255)
    @Column(name = "blue", nullable = false)
    private int blue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonManagedReference
    private User user;

    @Builder.Default
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
    private List<Expense> expenseList = new ArrayList<>();

}
