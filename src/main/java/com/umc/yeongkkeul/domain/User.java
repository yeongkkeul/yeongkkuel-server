package com.umc.yeongkkeul.domain;

import com.umc.yeongkkeul.domain.common.BaseEntity;
import com.umc.yeongkkeul.domain.enums.AgeGroup;
import com.umc.yeongkkeul.domain.enums.Job;
import com.umc.yeongkkeul.domain.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@DynamicUpdate
@DynamicInsert
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class User extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "oauth_type", nullable = false, length = 20)
    private String oauthType;

    @Column(name = "oauth_key", nullable = false, length = 255)
    private String oauthKey;

    @Column(name = "nickname", nullable = false, length = 50)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(name = "userRole", nullable = false)
    private UserRole userRole;

    @Column(name = "gender", nullable = false, length = 10)
    private String gender;

    @Enumerated(EnumType.STRING)
    @Column(name = "age_group", nullable = false)
    private AgeGroup ageGroup;

    @Enumerated(EnumType.STRING)
    @Column(name = "job", nullable = false)
    private Job job;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "referral_code", length = 6)
    private String referralCode;

    @Column(name = "reward_balance", nullable = false)
    private int rewardBalance;

    @Column(name = "status", nullable = false)
    private boolean status;

    @Column(name = "inactive_date")
    private LocalDateTime inactiveDate;

    @Column(name = "day_target_expenditure")
    private Integer dayTargetExpenditure; // 널 허용이기에 Wrapper 객체 사용

    @Column(name = "notification_agreed", nullable = false)
    private boolean notificationAgreed;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Reward> rewardList = new ArrayList<>();

}
