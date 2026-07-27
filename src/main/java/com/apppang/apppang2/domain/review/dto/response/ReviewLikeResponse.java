package com.apppang.apppang2.domain.review.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReviewLikeResponse {
    private final boolean liked;
    private final int helpCount;
}
