package com.apppang.appgang2.domain.user.controller;

import com.apppang.appgang2.domain.user.dto.SignupRequest;
import com.apppang.appgang2.domain.user.dto.SignupResponse;
import com.apppang.appgang2.domain.user.service.AuthService;
import com.apppang.appgang2.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor    //final 필드에 대해 롬북이 생성자를 자동으로 만들어 의존성을 주입
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    //회원가입
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<SignupResponse>> signup(@Valid @RequestBody SignupRequest signupRequest){

        //비즈니스 로직 완료 후 응답에 필요한 userId만 서비스로부터 받아옴
        Long savedUserId = authService.signup(signupRequest);

        SignupResponse signupResponse = new SignupResponse(savedUserId);

        //201 Created 상태와 JSON 응답 반환
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("회원가입이 완료되었습니다.",signupResponse));
    }

    //로그인, 따로 만들어뒀던 LoginController를 AuthController에 합칠 예정입니다.

}
