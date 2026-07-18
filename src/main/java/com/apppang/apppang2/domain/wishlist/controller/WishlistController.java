package com.apppang.apppang2.domain.wishlist.controller;

import com.apppang.apppang2.domain.wishlist.dto.WishlistRequest;
import com.apppang.apppang2.domain.wishlist.dto.WishlistResponse;
import com.apppang.apppang2.domain.wishlist.service.WishlistService;
import com.apppang.apppang2.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "WISHLIST")
@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
public class WishlistController {
    private final WishlistService wishlistService;

    //찜 목록 조회
    @Operation(summary = "찜 목록 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<WishlistResponse>> getWishlist(@AuthenticationPrincipal Long userId){
        WishlistResponse data = wishlistService.getWishlist(userId);
        return ResponseEntity.ok(ApiResponse.success("찜 목록 조회에 성공했습니다.",data));
    }

    //찜 추가
    @Operation(summary = "찜 추가")
    @PostMapping("/{productId}")
    public ResponseEntity<ApiResponse<Void>> addWishlist(@AuthenticationPrincipal Long userId, @PathVariable Long productId){

        wishlistService.addWishlist(userId, productId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("찜 목록에 추가되었습니다."));
    }

    //찜 삭제
    @Operation(summary = "찜 삭제")
    @DeleteMapping("/{productId}")
    public ResponseEntity<ApiResponse<Void>> deleteWishlist(@AuthenticationPrincipal Long userId, @PathVariable Long productId){
        wishlistService.deleteWishlist(userId, productId);  //서비스 삭제 로직 호출
        return ResponseEntity.ok(ApiResponse.success("찜 목록에서 삭제되었습니다."));
    }
}
