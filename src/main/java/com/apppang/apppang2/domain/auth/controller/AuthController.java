package com.apppang.apppang2.domain.auth.controller;

import com.apppang.apppang2.domain.auth.dto.request.FindIdRequest;
import com.apppang.apppang2.domain.auth.dto.response.EmailCheckResponse;
import com.apppang.apppang2.domain.auth.dto.response.FindIdResponse;
import com.apppang.apppang2.domain.auth.dto.request.SignupRequest;
import com.apppang.apppang2.domain.auth.dto.response.SignupResponse;
import com.apppang.apppang2.domain.auth.dto.request.LoginRequest;
import com.apppang.apppang2.domain.auth.dto.response.LoginResponse;
import com.apppang.apppang2.domain.auth.service.AuthService;
import com.apppang.apppang2.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Auth")
@RestController
@RequiredArgsConstructor    //final 필드에 대해 롬북이 생성자를 자동으로 만들어 의존성을 주입
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @Value("${jwt.refresh-token.expiration}")
    private long refreshTokenExpirationMs;

    //회원가입
    @Operation(summary = "회원가입")
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

    //로그인 API
    @Operation(summary = "로그인")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {

        //서비스 로직 호출
        LoginResponse loginResponse = authService.login(request);

        //쿠키 수명은 초 단위로 저장
        long maxAgeSeconds = refreshTokenExpirationMs/1000;

        //refreshToken cookie에 저장
        ResponseCookie cookie = ResponseCookie.from("refreshToken", loginResponse.getRefreshToken())
                .httpOnly(true)           // JavaScript 접근 차단
                .path("/")                // 모든 경로에서 쿠키 전송
                .maxAge(maxAgeSeconds)    // 만료 시간 설정
                //.secure(true)            // 운영 환경 배포 시 활성화
                .sameSite("Lax")          // CSRF 공격 방지
                .build();


        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(ApiResponse.success("로그인 성공", loginResponse));
    }

    //아이디 찾기
    @Operation(summary = "아이디 찾기")
    @PostMapping("/find-id")
    public ResponseEntity<ApiResponse<FindIdResponse>> findUserId(@Valid @RequestBody FindIdRequest findIdRequest){
        //JSON을 자바 객체로 바꾸어 그 내용을 검사하고
        //요청받은 이름과 휴대폰 번호로 유저 아이디를 조회
        FindIdResponse data = authService.findId(findIdRequest);
        return ResponseEntity.ok(ApiResponse.success("아이디를 찾았습니다.",data));
    }

    //이메일 중복 확인
    @Operation(summary = "이메일 중복확인")
    @GetMapping("/email-check")
    //@RequestParam : 쿼리 파라미터로 전달된 값을 String email에 매핑
    public ResponseEntity<ApiResponse<EmailCheckResponse>> checkEmail(@RequestParam String email){

        boolean isAvailable = authService.isEmailAvailable(email);

        //boolean 값을 JSON 형식으로 응답하기 위해 객체 생성
        EmailCheckResponse data = new EmailCheckResponse(isAvailable);

        String message = isAvailable ? "사용 가능한 이메일입니다." : "이미 사용 중인 이메일입니다.";

        return ResponseEntity.ok(ApiResponse.success(message, data));
    }

    @Operation(summary = "로그아웃")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(@AuthenticationPrincipal Long userId){
        //JwtUtil이 SpringContext에 넣어둔 결과를 꺼내서 userId에 주입

        //유저Id 기준으로 DB의 Refresh Token을 삭제
        authService.logout(userId);

        return ResponseEntity.ok(ApiResponse.success("로그아웃되었습니다."));
    }
}