package com.umc.yeongkkeul.domain.mapping;

import com.umc.yeongkkeul.domain.ChatRoom;
import com.umc.yeongkkeul.domain.User;
import com.umc.yeongkkeul.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;

import java.time.DateTimeException;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ChatRoomMembership extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // user_id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // chat_room_id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatroom;

    // 방장 여부
    @Column(name = "is_host", nullable = false)
    private Boolean isHost;

    // 추방 여부
    @Column(name = "is_banned", nullable = false)
    private Boolean isBanned;

    // 참여 날짜
    @CreatedDate
    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;

    // 떠난 날짜
    @Column(name = "left_at")
    private LocalDateTime leftAt;

    // 추방 날짜
    @Column(name = "banned_at")
    private LocalDateTime bannedAt;

    @Column
    private Long userScore;
}