package com.apppang.apppang2.domain.banner.dto;

import com.apppang.apppang2.domain.banner.entity.Banner;
import lombok.Getter;

@Getter
public class BannerResponse {
    private Long bannerId;
    private String title;
    private String imageUrl;
    private String targetUrl;

    //Banner 엔티티를 BannerResponse로 변환하기 위한 생성자
    public BannerResponse(Banner banner){
        this.bannerId = banner.getId();
        this.title = banner.getTitle();
        this.imageUrl = banner.getImageUrl();
        this.targetUrl = banner.getTargetUrl();
    }
}
