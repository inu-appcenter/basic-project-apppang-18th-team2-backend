package com.apppang.apppang2.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateMyInfoRequest {

    @NotBlank(message = "이름은 필수입니다.")
    @Pattern(regexp = "^[가-힣a-zA-Z]{2,}$", message = "입력값이 올바르지 않습니다.")
    private String name;

    @NotBlank(message = "휴대폰 번호는 필수입니다.")
    @Pattern(regexp = "^010\\d{8}$", message = "입력값이 올바르지 않습니다.")
    private String phone;
}