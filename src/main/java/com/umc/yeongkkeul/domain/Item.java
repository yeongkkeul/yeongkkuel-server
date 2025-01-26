package com.umc.yeongkkeul.domain;

import com.umc.yeongkkeul.domain.common.BaseEntity;
import com.umc.yeongkkeul.domain.enums.ItemType;
import jakarta.persistence.*;

/**
 * 상점 아이템
 */
@Entity
public class Item extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", length = 20, nullable = false)
    private String name;

    @Column(name = "type", nullable = false)
    @Enumerated(EnumType.STRING)
    private ItemType type;

    @Column(name = "price", nullable = false)
    private Integer price;

    // null 값이면 프론트에서 기본 이미지로 등록
    @Column(name = "image_url")
    private String imageUrl;

    public String getName(){
        return name;
    }
    public ItemType getType(){
        return type;
    }
    public String getImgUrl(){
        return imageUrl;
    }
}

