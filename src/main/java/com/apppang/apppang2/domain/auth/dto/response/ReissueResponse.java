package com.apppang.apppang2.domain.auth.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReissueResponse {
    private String accessToken;
}
