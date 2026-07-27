package com.apppang.apppang2.domain.review.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;

@Getter
public class ReviewUpdateRequest {
    @NotNull(message = "별점은 필수입니다.")
    @DecimalMin(value = "1.0")
    @DecimalMax(value = "5.0")
    private Double rating;

    @Size(min = 10, max = 1000, message = "리뷰는 10자 이상, 1000자 이하로 작성해주세요")
    private String content;
}
