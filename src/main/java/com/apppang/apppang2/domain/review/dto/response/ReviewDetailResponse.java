package com.apppang.apppang2.domain.review.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ReviewDetailResponse {
    private Long reviewId;
    private String userName;
    private Double rating;
    private String content;
    private List<String> images;
    private String createdAt;
    private int helpCount;
    private boolean helped;             //현재 로그인한 유저가 이 리뷰에 도움돼요를 눌렀는지 여부
}
