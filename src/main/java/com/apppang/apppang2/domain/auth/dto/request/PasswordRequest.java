package com.apppang.apppang2.domain.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class PasswordRequest {
    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    //이메일 입력하고 재설정 링크 요청할 때
    public static class Request{
        @NotBlank(message = "이메일을 입력해주세요.")
        @Email(message = "올바른 이메일 형식이 아닙니다.")
        private String email;
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    //링크 클릭 후 새 비밀번호 입력해서 변경할 때
    public static class Confirm{
        @NotBlank(message = "토큰이 없습니다.")
        private String token;           //주소창에 있는 토큰 값

        @NotBlank(message = "영문+숫자의 형식(8~20자)")
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,20}$", message = "영문+숫자의 형식(8~20자)")
        private String newPassword;     //새 비밀번호
    }
}
