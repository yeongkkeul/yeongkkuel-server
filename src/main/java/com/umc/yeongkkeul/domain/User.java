package com.umc.yeongkkeul.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.umc.yeongkkeul.domain.common.BaseEntity;
import com.umc.yeongkkeul.domain.enums.AgeGroup;
import com.umc.yeongkkeul.domain.enums.Job;
import com.umc.yeongkkeul.domain.enums.UserRole;
import com.umc.yeongkkeul.domain.mapping.ChatRoomMembership;
import com.umc.yeongkkeul.domain.mapping.Purchase;
import com.umc.yeongkkeul.domain.mapping.UserTerms;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
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

    @Email
    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "referral_code", length = 6)
    private String referralCode;

    // 보유 리워드
    @Column(name = "reward_balance", nullable = false)
    private int rewardBalance;

    @Column(name = "status", nullable = false)
    private boolean status;

    @Column(name = "inactive_date")
    private LocalDateTime inactiveDate;

    @Column(name = "profile_url")
    private String imageUrl;

    // 하루 목표 지출액
    @Column(name = "day_target_expenditure")
    private Integer dayTargetExpenditure; // 널 허용이기에 Wrapper 객체 사용

    @Column(name = "notification_agreed", nullable = false)
    private boolean notificationAgreed;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Reward> rewardList = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<ChatRoomMembership> chatRoomMembershipList = new ArrayList<>();

    /*
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Message> messageList = new ArrayList<>();

     */

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<UserTerms> userTermList = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Expense> expenseList = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonBackReference
    private List<Category> categoryList = new ArrayList<>();

    // 연관관계 편의 메서드 추가
    public void addCategory(Category category) {
        categoryList.add(category);
        category.setUser(this);
    }

    public void removeCategory(Category category) {
        categoryList.remove(category);
        category.setUser(null);
    }

    public void updateProfile(String nickname, String gender, AgeGroup ageGroup, Job job, String profileImageUrl) {
        this.nickname = nickname;
        this.gender = gender;
        this.ageGroup = ageGroup;
        this.job = job;
        this.imageUrl = profileImageUrl;
    }

    // 유저의 지출 내역 삭제
    public void removeExpense(Expense expense) {
        expenseList.remove(expense);
        expense.setUser(null);
    }

    // 유저의 하루 목표 지출액 설정
    public void setDayTargetExpenditure(Integer dayTargetExpenditure) {
        this.dayTargetExpenditure = dayTargetExpenditure;
    }

    public AgeGroup getAge() {
        return ageGroup;
    }
}
