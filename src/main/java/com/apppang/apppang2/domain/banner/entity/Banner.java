package com.apppang.apppang2.domain.banner.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
//복합 인덱스 생성
@Table(name = "banners",
        indexes = {
                @Index(name = "idx_banner_active_order", columnList = "is_active, display_order")
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)  //JPA만 접근할 수 있도록 protected로 제한
public class Banner {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "banner_id")
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 512)
    private String imageUrl;

    @Column(nullable = false, length = 512)
    private String targetUrl;   //클릭 시 이동할 URL

    @Column(nullable = false)
    private int displayOrder;   //표시순서(낮을수록 먼저 노출)

    @Column(name = "is_active", nullable = false)
    private boolean active;   //활성화 여부

    @CreationTimestamp  //객체가 생성될 때 자동으로 현재시간 넣어줌
    @Column(name = "created_at", nullable = false, columnDefinition = "datetime(6) default CURRENT_TIMESTAMP(6)")
    private LocalDateTime createdAt;

    @Builder
    public Banner(String title, String imageUrl, String targetUrl, int displayOrder, boolean active){
        this.title = title;
        this.imageUrl = imageUrl;
        this.targetUrl = targetUrl;
        this.displayOrder = displayOrder;
        this.active = active;
    }
}
