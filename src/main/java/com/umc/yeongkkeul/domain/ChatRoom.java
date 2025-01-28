package com.umc.yeongkkeul.domain;

import com.umc.yeongkkeul.domain.common.BaseEntity;
import com.umc.yeongkkeul.domain.enums.AgeGroup;
import com.umc.yeongkkeul.domain.enums.Job;
import com.umc.yeongkkeul.domain.mapping.ChatRoomMembership;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ChatRoom extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 채팅방 제목
    @Column(name = "title", nullable = false, length = 18)
    private String title;

    // 비밀번호
    @Column(name = "password", length = 4)
    private String password;

    // 설명
    @Column(name = "description", length = 200)
    private String description;

    // 최대 참여인원 - 최대 100명
    @Column(name = "max_participants", nullable = false)
    @Max(100)
    private Integer maxParticipants;

    // 참여 인원 수 - 최대 maxParticipants명
    @Column(name = "participation_count", nullable = false)
    @Min(0) // 음수가 될 수 없도록
    private Integer participationCount;

    // 참여 나이 필터
    @Enumerated(EnumType.STRING)
    @Column(name = "age_group_filter")
    private AgeGroup ageGroupFilter;

    // 작업 필터
    @Enumerated(EnumType.STRING)
    @Column(name = "job_filter")
    private Job jobFilter;

    // 하루 목표지출액 필터
    @Column(name="daily_spending_goal_filter", nullable = false)
    @Max(99999999)
    private Integer dailySpendingGoalFilter;

    // 프로필 이미지
    @Column(name = "image_url")
    private String imageUrl;

    @OneToMany(mappedBy = "chatroom", cascade = CascadeType.ALL)
    private List<ChatRoomMembership> chatRoomMembershipList = new ArrayList<>();

    /*
    @OneToMany(mappedBy = "chatroom", cascade = CascadeType.ALL)
    private List<Message> messageList = new ArrayList<>();

     */
}