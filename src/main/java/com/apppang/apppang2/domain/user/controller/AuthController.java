package com.apppang.apppang2.domain.user.controller;

import com.apppang.apppang2.domain.user.dto.EmailCheckResponse;
import com.apppang.apppang2.domain.user.dto.SignupRequest;
import com.apppang.apppang2.domain.user.dto.SignupResponse;
import com.apppang.apppang2.domain.user.service.AuthService;
import com.apppang.apppang2.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    //이메일 중복 확인
    @GetMapping("/email-check")
    //@RequestParam : 쿼리 파라미터로 전달된 값을 String email에 매핑
    public ResponseEntity<ApiResponse<EmailCheckResponse>> checkEmail(@RequestParam String email){

        boolean isAvailable = authService.isEmailAvailable(email);

        //boolean 값을 JSON 형식으로 응답하기 위해 객체 생성
        EmailCheckResponse data = new EmailCheckResponse(isAvailable);

        String message = isAvailable ? "사용 가능한 이메일입니다." : "이미 사용 중인 이메일입니다.";

        return ResponseEntity.ok(ApiResponse.success(message, data));
    }

}
