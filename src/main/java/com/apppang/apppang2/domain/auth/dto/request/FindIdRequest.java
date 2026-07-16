package com.apppang.apppang2.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor  //JSON -> DTO 반환 시 사용
public class FindIdRequest{
    @NotBlank(message = "이름을 입력해주세요.")
    private String name;
    @NotBlank(message = "휴대폰번호를 입력해주세요.")
    private String phone;
}
