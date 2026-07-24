package com.apppang.apppang2.domain.auth.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EmailCheckResponse {
    private boolean available;    //이메일 사용 가능 여부
}
