package com.apppang.apppang2.domain.review.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ReviewListResponse {
    private final List<ReviewDetailResponse> reviews;
    private final int page;
    private final boolean hasNext;
}
