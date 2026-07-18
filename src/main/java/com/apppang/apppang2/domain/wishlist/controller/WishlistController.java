package com.apppang.apppang2.domain.wishlist.controller;

import com.apppang.apppang2.domain.wishlist.dto.WishlistResponse;
import com.apppang.apppang2.domain.wishlist.service.WishlistService;
import com.apppang.apppang2.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
public class WishlistController {
    private final WishlistService wishlistService;

    @Operation(summary = "찜 목록 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<WishlistResponse>> getWishlist(@AuthenticationPrincipal Long userId){
        WishlistResponse data = wishlistService.getWishlist(userId);
        return ResponseEntity.ok(ApiResponse.success("찜 목록 조회에 성공했습니다.",data));
    }
}
