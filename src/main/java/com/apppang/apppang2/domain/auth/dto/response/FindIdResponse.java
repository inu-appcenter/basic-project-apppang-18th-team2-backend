package com.apppang.apppang2.domain.auth.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FindIdResponse {
    private String email;
}
