package com.apppang.appgang2.domain.banner;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "banners")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)  //JPA만 접근할 수 있도록 protected로 제한
public class Banner {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "banner_id")
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String imageUrl;

    @Column(nullable = false)
    private String targetUrl;   //클릭 시 이동할 URL

    @Column(nullable = false)
    private int displayOrder;   //표시순서(낮을수록 먼저 노출)

    @Column(nullable = false)
    private boolean isActive;   //활성화 여부

    @Builder
    public Banner(String title, String imageUrl,
                  String targetUrl, int displayOrder, boolean isActive){
        this.title = title;
        this.imageUrl = imageUrl;
        this.targetUrl = targetUrl;
        this.displayOrder = displayOrder;
        this.isActive = isActive;
    }
}
