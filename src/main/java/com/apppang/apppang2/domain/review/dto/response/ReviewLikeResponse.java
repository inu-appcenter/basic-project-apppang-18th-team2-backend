package com.apppang.apppang2.domain.review.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReviewLikeResponse {
    private boolean liked;
    private int helpCount;
}
