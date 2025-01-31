package com.umc.yeongkkeul.domain;

import com.umc.yeongkkeul.domain.common.BaseEntity;
import com.umc.yeongkkeul.domain.enums.NotificationType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class Notification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // TODO: 필요한 타입 있으면 NotificationType에서 추가하시면 됩니다.
    @Column(name = "notification_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private NotificationType notificationType;

    @Column(name = "notification_content", nullable = false)
    private String notificationContent;

    @Column(name = "target_url")
    private String targetUrl;
}