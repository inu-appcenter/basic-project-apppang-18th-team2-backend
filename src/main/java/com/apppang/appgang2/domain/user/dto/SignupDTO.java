package com.apppang.appgang2.domain.user.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SignupDTO {
    @NotBlank(message = "올바른 이메일 형식을 입력해주세요.")
    @Email(message = "올바른 이메일 형식을 입력해주세요.")
    private String email;

    @NotBlank(message = "영문+숫자의 형식(8~20자)")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,20}$",message = "영문+숫자의 형식(8~20자)")
    private String password;

    @NotBlank(message = "이름을 정확히 입력하세요.")
    @Pattern(regexp = "^[가-힣a-zA-Z]{2,}$", message = "이름을 정확히 입력하세요.")
    private String name;

    @NotBlank(message = "휴대폰 번호를 올바르게 입력해주세요.")
    private String phone;

    @NotNull(message = "필수 항목에 모두 동의해주세요")
    @AssertTrue(message = "필수 항목에 모두 동의해주세요")   //null이 아니면 무조건 True여야 함
    private Boolean agreeRequiredTerms; //필수 약관 동의 여부

    //선택 약관이므로 값을 보내지 않을(null) 경우를 대비해 기본값을 false로 설정
    private Boolean agreeMarketing = false; //마케팅 수신 동의 여부
}
