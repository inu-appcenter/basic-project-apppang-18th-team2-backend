package com.apppang.apppang2.domain.review.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ReviewListResponse {
    private List<ReviewDetailResponse> reviews;
    private int page;
    private boolean hasNext;
}
