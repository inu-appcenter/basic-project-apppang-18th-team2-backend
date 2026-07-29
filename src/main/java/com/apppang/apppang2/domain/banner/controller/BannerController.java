package com.apppang.apppang2.domain.banner.controller;

import com.apppang.apppang2.domain.banner.service.BannerService;
import com.apppang.apppang2.domain.banner.dto.response.BannerResponse;
import com.apppang.apppang2.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "BANNER")
@RestController
@RequiredArgsConstructor
public class BannerController {
    private final BannerService bannerService;

    //배너 조회
    @Operation(summary = "메인 배너 조회")
    @GetMapping("/api/banners")
    //서비스가 준 List<BannerResponse>를 ResponseEntity로 포장하여 응답
    public ResponseEntity<ApiResponse<List<BannerResponse>>> getBanners(){
        List<BannerResponse> banners = bannerService.getActiveBanners();

        return ResponseEntity.ok(ApiResponse.success("메인 배너 조회에 성공했습니다.",banners));
    }
}
