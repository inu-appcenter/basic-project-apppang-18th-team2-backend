package com.apppang.apppang2.domain.review.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class ReviewCreateRequest {
    @NotNull(message = "주문 번호는 필수입니다.")
    private Long orderId;

    @NotNull(message = "상품 번호는 필수입니다.")
    private Long productId;

    @NotNull(message = "별점은 필수입니다.")
    @DecimalMin(value = "1.0", message = "별점은 최소 1.0점 이상이어야 합니다.")
    @DecimalMax(value = "5.0", message = "별점은 최대 5.0점 이하이어야 합니다.")
    private Double rating;      //별점 입력을 안하면 null이 반환되어 에러 반환

    @Size(min = 10, max = 1000, message = "리뷰는 10자 이상, 1000자 이하로 작성해주세요")
    private String content;

    private List<String> imageUrls;
}
