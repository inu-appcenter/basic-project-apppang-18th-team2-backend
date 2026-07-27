package com.apppang.apppang2.domain.banner.dto.response;

import com.apppang.apppang2.domain.banner.entity.Banner;
import lombok.Getter;

@Getter
public class BannerResponse {
    private final Long bannerId;
    private final String title;
    private final String imageUrl;
    private final String targetUrl;

    //Banner 엔티티를 BannerResponse로 변환하기 위한 생성자
    public BannerResponse(Banner banner){
        this.bannerId = banner.getId();
        this.title = banner.getTitle();
        this.imageUrl = banner.getImageUrl();
        this.targetUrl = banner.getTargetUrl();
    }
}
