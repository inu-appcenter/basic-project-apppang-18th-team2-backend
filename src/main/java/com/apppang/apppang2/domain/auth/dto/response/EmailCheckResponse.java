package com.apppang.apppang2.domain.auth.dto.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class EmailCheckResponse {

    private final boolean available;    //이메일 사용 가능 여부
}
