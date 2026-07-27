package com.apppang.apppang2.domain.auth.service;

import com.apppang.apppang2.domain.auth.dto.request.FindIdRequest;
import com.apppang.apppang2.domain.auth.dto.response.TokenDto;
import com.apppang.apppang2.domain.auth.dto.response.FindIdResponse;
import com.apppang.apppang2.domain.auth.dto.request.LoginRequest;
import com.apppang.apppang2.domain.auth.dto.response.LoginResponse;
import com.apppang.apppang2.domain.auth.dto.request.SignupRequest;
import com.apppang.apppang2.domain.user.entity.Role;
import com.apppang.apppang2.domain.user.entity.User;
import com.apppang.apppang2.domain.user.repository.UserRepository;
import com.apppang.apppang2.global.exception.CustomException;
import com.apppang.apppang2.global.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.UUID;


@Slf4j
@RequiredArgsConstructor
@Service
public class AuthService {
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final StringRedisTemplate redisTemplate;
    private final MailService mailService;


    //컨트롤러의 최종 응답을 위해 DB에 저장된 후 발급된 userId(Long)를 반환
    @Transactional
    public Long signup(SignupRequest signupRequest) {
        String email = signupRequest.getEmail();
        String password = signupRequest.getPassword();

        //이메일 중복 검사
        Boolean isExist = userRepository.existsByEmailAndDeletedFalse(email);

        //중복일 경우 예외 처리
        if (isExist) {
            throw new CustomException(HttpStatus.CONFLICT, "이미 사용중인 이메일입니다.");
        }

        //받은 DTO를 User 엔티티 형태로 옮겨담는 과정
        User user = User.builder()
                .email(email)
                .password(bCryptPasswordEncoder.encode(password))   //비밀번호 암호화
                .name(signupRequest.getName())
                .phone(signupRequest.getPhone())
                .agreeRequiredTerms(signupRequest.isAgreeRequiredTerms())
                .agreeMarketing(signupRequest.isAgreeMarketing())
                .role(Role.USER)
                .deleted(false)
                .build();

        //엔티티를 실제 DB에 저장
        User savedUser = userRepository.save(user);

        //저장 후 DB에서 발급된 기본키(유저아이디)를 꺼내서 컨트롤러로 반환
        return savedUser.getId();
    }

    public LoginResponse login(LoginRequest request) {

        //사용자 확인 (없으면 예외 발생)
        User user = userRepository.findByEmailAndDeletedFalse(request.getEmail())
                .orElseThrow(() -> new CustomException(HttpStatus.UNAUTHORIZED, "가입되지 않은 이메일입니다."));

        //비밀번호 검증 (암호화된 비번과 비교)
        if (!bCryptPasswordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new CustomException(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        //토큰 생성
        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getEmail());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());

        //refreshToken을 Redis에 저장, 14일 후 자동 만료
        redisTemplate.opsForValue().set("refresh:" + user.getId(), refreshToken, Duration.ofDays(14));

        //로그인 응답 DTO 생성 및 반환
        return LoginResponse.builder()
                .accessToken(accessToken)               //토큰에 데이터 넣기
                .refreshToken(refreshToken)
                .user(LoginResponse.UserInfo.builder()  //user에 UserInfo 넣기
                        .userId(user.getId())
                        .email(user.getEmail())
                        .build())
                .build();
    }

    //이메일 중복 검사
    public boolean isEmailAvailable(String email) {
        //중복이라면 !true가 되어 false 리턴
        //사용가능하다면 !false가 되어 true 리턴
        return !userRepository.existsByEmailAndDeletedFalse(email);
    }

    //아이디 찾기
    public FindIdResponse findId(FindIdRequest request){
        //이름과 휴대폰번호로 유저 엔티티 조회
        User user = userRepository.findByNameAndPhoneAndDeletedFalse(request.getName(),request.getPhone())
                .orElseThrow(()->new CustomException(HttpStatus.NOT_FOUND, "일치하는 회원 정보를 찾을 수 없습니다."));

        //조회된 유저의 이메일 반환
        return FindIdResponse.builder()
                .email(user.getEmail())
                .build();
    }

    //로그아웃: 저장된 refreshToken 삭제
    public void logout(Long userId){
        redisTemplate.delete("refresh:" + userId);
    }

    //비밀번호 찾기
    public void sendResetMail(String email){
        //유저 찾기
        User user = userRepository.findByEmail(email)
                .orElseThrow(()->new CustomException(HttpStatus.NOT_FOUND, "일치하는 회원 정보를 찾을 수 없습니다."));

        //재설정 토큰을 Redis에 10분 만료기간으로 저장 (key=토큰, value=userId)
        String token = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set("pwreset:" + token, String.valueOf(user.getId()), Duration.ofMinutes(10));

        String resetLink = "http://localhost:5173/password-reset?token=" + token;
        mailService.sendResetPAsswordEmail(user.getEmail(), resetLink);
    }

    //토큰 검증 및 새 비밀번호로 변경 요청
    @Transactional
    public void resetPassword(String token, String newPassword){
        //조회하면서 만료 검사도 한다. 없으면 무효였거나 기간만료로 이미 사라진 것
        String userIdStr = redisTemplate.opsForValue().get("pwreset:" + token);
        if (userIdStr == null){
            throw new CustomException(HttpStatus.BAD_REQUEST, "유효하지 않거나 만료된 재설정 링크입니다.");
        }

        User user = userRepository.findById(Long.parseLong(userIdStr))
                .orElseThrow(() -> new CustomException(HttpStatus.NOT_FOUND, "회원 정보를 찾을 수 없습니다."));

        //새 비밀번호 암호화해서 User 엔티티 업데이트
        String encodedPassword = bCryptPasswordEncoder.encode(newPassword);
        user.updatePassword(encodedPassword);

        //사용 완료된 토큰 삭제
        redisTemplate.delete("pwreset:" + token);
    }

    //토큰 재발급
    public TokenDto reissueToken(String oldRefreshToken){
        //서명 검증 겸 userId 추출 — 위조 토큰은 여기서 예외 발생
        Long userId = Long.valueOf(jwtUtil.getClaims(oldRefreshToken).getSubject());

        //Redis 저장본과 대조, 없으면 로그아웃됐거나 14일 기간만료된 것
        String saved = redisTemplate.opsForValue().get("refresh:" + userId);
        if (saved == null || !saved.equals(oldRefreshToken)){
            throw new CustomException(HttpStatus.UNAUTHORIZED, "유효하지 않거나 만료된 Refresh Token입니다.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(()->new CustomException(HttpStatus.NOT_FOUND,"회원 정보를 찾을 수 없습니다."));

        //새로운 토큰 생성
        String newAccessToken = jwtUtil.generateAccessToken(user.getId(), user.getEmail());
        String newRefreshToken = jwtUtil.generateRefreshToken(user.getId());

        //덮어쓰기 = 기존 토큰 무효화 + 새 토큰 저장 (delete+save 두 단계가 한 줄로)
        redisTemplate.opsForValue().set("refresh:" + userId, newRefreshToken, Duration.ofDays(14));

        return TokenDto.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();
    }

}