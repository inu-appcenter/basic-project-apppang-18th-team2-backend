package com.apppang.apppang2.domain.auth.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
//토큰 재발급 시 서비스 계층에서 발급된 토큰들을 컨트롤러로 전달하는 DTO
public class TokenDto {
    private String accessToken;
    private String refreshToken;
}
