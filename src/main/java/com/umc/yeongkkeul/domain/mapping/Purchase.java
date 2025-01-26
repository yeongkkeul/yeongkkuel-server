package com.umc.yeongkkeul.domain.mapping;

import com.umc.yeongkkeul.domain.Item;
import com.umc.yeongkkeul.domain.User;
import com.umc.yeongkkeul.domain.common.BaseEntity;
import com.umc.yeongkkeul.domain.enums.ItemType;
import com.umc.yeongkkeul.repository.ItemRepository;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * 유저가 상점에서 구매한 아이템 이력
 * User-Item의 중간 테이블
 */
@Entity
public class Purchase extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @Column(name = "type", nullable = false)
    private ItemType type;

    @Column(name = "purchased_at", nullable = false)
    private LocalDateTime purchasedAt;

    @Column(name = "used_reward", nullable = false)
    private Integer usedReward;

    @Column(name = "is_used", nullable = false)
    private Boolean isUsed;

    public Item getItem() {
        return item;
    }
    public Boolean getIsUsed(){
        return isUsed;
    }
}
