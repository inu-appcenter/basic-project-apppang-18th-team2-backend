package com.apppang.apppang2.domain.review.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;

import java.util.List;

@Getter
public class ReviewCreateRequest {

    @NotNull(message = "주문 상세 번호는 필수입니다.")
    private Long orderDetailId;

    @NotNull(message = "별점은 필수입니다.")
    @DecimalMin(value = "1.0")
    @DecimalMax(value = "5.0")
    private Double rating;      //별점 입력을 안하면 null이 반환되어 에러 반환

    @NotBlank(message = "리뷰 내용을 입력해주세요.")
    @Size(min = 10, max = 500, message = "리뷰는 10자 이상, 500자 이하로 작성해주세요")
    private String content;

    private List<String> imageUrls;
}
