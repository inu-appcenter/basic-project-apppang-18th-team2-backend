package com.apppang.apppang2.domain.review.controller;

import com.apppang.apppang2.domain.review.dto.request.ReviewCreateRequest;
import com.apppang.apppang2.domain.review.dto.request.ReviewUpdateRequest;
import com.apppang.apppang2.domain.review.dto.response.ReviewCreateResponse;
import com.apppang.apppang2.domain.review.dto.response.ReviewLikeResponse;
import com.apppang.apppang2.domain.review.dto.response.ReviewListResponse;
import com.apppang.apppang2.domain.review.service.ReviewService;
import com.apppang.apppang2.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "REVIEW")
@RestController
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    //리뷰작성
    @Operation(summary = "리뷰 작성")
    @PostMapping("/api/reviews")
    public ResponseEntity<ApiResponse<ReviewCreateResponse>> createReview(@AuthenticationPrincipal Long userId, @Valid @RequestBody ReviewCreateRequest request){
        Long reviewId = reviewService.createReview(userId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("리뷰가 작성되었습니다.", new ReviewCreateResponse(reviewId)));
    }

    //리뷰조회
    @Operation(summary = "리뷰 조회")
    @GetMapping("/api/products/{productId}/reviews")
    //인증된 유저Id(비회원은 null)가 요청받은 특정 상품의 리뷰를 한 페이지당 10개의 리뷰씩 묶어서 조회
    public ResponseEntity<ApiResponse<ReviewListResponse>> getReviews(@PathVariable Long productId, @AuthenticationPrincipal Long userId, @PageableDefault(size=10) Pageable pageable){
        ReviewListResponse response = reviewService.getReviews(productId, userId, pageable);

        return ResponseEntity.ok(ApiResponse.success("리뷰조회에 성공했습니다.",response));
    }

    //리뷰 수정
    @Operation(summary = "리뷰 수정")
    @GetMapping("/api/reviews/{reviewId}")
    public ResponseEntity<ApiResponse<Void>> updateReviews(@PathVariable Long reviewId, @AuthenticationPrincipal Long userId, @Valid @RequestBody ReviewUpdateRequest request){
        reviewService.updateReview(reviewId, userId, request);

        return ResponseEntity.ok(ApiResponse.success("리뷰가 수정되었습니다."));
    }

    //리뷰 삭제
    @Operation(summary = "리뷰 삭제")
    @GetMapping("/api/reviews/{reviewId}")
    public ResponseEntity<ApiResponse<Void>> deleteReviews(@PathVariable Long reviewId, @AuthenticationPrincipal Long userId){
        reviewService.deleteReview(reviewId, userId);

        return ResponseEntity.ok(ApiResponse.success("리뷰가 삭제되었습니다."));
    }

    //도움돼요
    @Operation(summary = "리뷰 삭제")
    @PostMapping("/api/reviews/{reviewId}/likes")
    public ResponseEntity<ApiResponse<ReviewLikeResponse>> helpedReviews(@PathVariable Long reviewId, @AuthenticationPrincipal Long userId){
        ReviewLikeResponse response = reviewService.helpedReview(reviewId, userId);

        String message = response.isLiked() ? "도움이 돼요를 추가했습니다." : "도움이 돼요를 취소했습니다.";
        return ResponseEntity.ok(ApiResponse.success(message, response));
    }

}
