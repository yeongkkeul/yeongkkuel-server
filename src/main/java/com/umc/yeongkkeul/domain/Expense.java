package com.umc.yeongkkeul.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.umc.yeongkkeul.domain.common.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Expense extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 지출 기록에 해당되는 날짜
    @Column(name = "day", nullable = false)
    private LocalDate day;

    @Column(name = "content", length = 20)
    private String content;

    @Column(name = "amount", nullable = false)
    private int amount;

    // true 이면 무지출
    @Column(name = "is_no_spending", nullable = false)
    private boolean isNoSpending;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "is_send")
    private Boolean isSend;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    public Boolean getIsNoSpending() {
        return isNoSpending; // true이면 무지출
    }
}
