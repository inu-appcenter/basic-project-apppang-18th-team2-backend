package com.apppang.apppang2.domain.auth.service;

import com.apppang.apppang2.domain.auth.dto.request.FindIdRequest;
import com.apppang.apppang2.domain.auth.dto.response.TokenDto;
import com.apppang.apppang2.domain.auth.dto.response.FindIdResponse;
import com.apppang.apppang2.domain.auth.dto.request.LoginRequest;
import com.apppang.apppang2.domain.auth.dto.response.LoginResponse;
import com.apppang.apppang2.domain.auth.dto.request.SignupRequest;
import com.apppang.apppang2.domain.auth.entity.PasswordResetToken;
import com.apppang.apppang2.domain.auth.entity.RefreshToken;
import com.apppang.apppang2.domain.auth.repository.PasswordResetTokenRepository;
import com.apppang.apppang2.domain.auth.repository.RefreshTokenRepository;
import com.apppang.apppang2.domain.user.entity.Role;
import com.apppang.apppang2.domain.user.entity.User;
import com.apppang.apppang2.domain.user.repository.UserRepository;
import com.apppang.apppang2.global.exception.CustomException;
import com.apppang.apppang2.global.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class AuthService {
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final MailService mailService;

    @Value("${jwt.refresh-token.expiration}")
    private long refreshTokenExpirationMs;


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
                .role(Role.USER) // swagger 테스트를 위해 role를 추가
                .deleted(false) // 회원탈퇴 hard 삭제 X
                .build();

        //엔티티를 실제 DB에 저장
        User savedUser = userRepository.save(user);

        //저장 후 DB에서 발급된 기본키(유저아이디)를 꺼내서 컨트롤러로 반환
        return savedUser.getId();
    }

    @Transactional
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

        //해당 유저가 이전에 발급받은 Refresh Token이 남아있다면 DB에서 삭제
        refreshTokenRepository.deleteByUserId(user.getId());

        //새 Refresh Token 엔티티 생성
        RefreshToken tokenEntity = RefreshToken.builder()
                .userId(user.getId())
                .refreshToken(refreshToken)
                .expiredAt(LocalDateTime.now().plus(refreshTokenExpirationMs, ChronoUnit.MILLIS))
                .build();

        refreshTokenRepository.save(tokenEntity);

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

    @Transactional      //삭제하는 작업이므로 안전하게 실행
    //로그아웃
    public void logout(Long userId){
        //유저Id를 기준으로 DB에 저장된 Refresh Token을 삭제
        try{
            refreshTokenRepository.deleteByUserId(userId);
            log.info("유저 {}의 RefreshToken을 성공적으로 삭제했습니다.", userId);
        }catch(Exception e){
            log.error("유저 {}의 RefreshToken 삭제 중 오류 발생: {}", userId, e.getMessage());
        }
    }

    //비밀번호 찾기
    @Transactional
    public void sendResetMail(String email){
        //유저 찾기
        User user = userRepository.findByEmail(email)
                .orElseThrow(()->new CustomException(HttpStatus.NOT_FOUND, "일치하는 회원 정보를 찾을 수 없습니다."));

        //기존에 발급된 유요한 토큰이 있다면 삭제
        passwordResetTokenRepository.deleteByUser(user);
        passwordResetTokenRepository.flush();       //삭제부터 하고 insert 하기

        //랜덤한 UUID 토큰 생성
        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiryDate(LocalDateTime.now().plusMinutes(10))    //만료시간 10분
                .build();
        passwordResetTokenRepository.save(resetToken);

        String resetLink = "http://localhost:5173/password-reset?token=" + token;
        mailService.sendResetPAsswordEmail(user.getEmail(), resetLink);

    }

    //토큰 검증 및 새 비밀번호로 변경 요청
    @Transactional
    public void resetPassword(String token, String newPassword){
        //토큰이 DB에 있는지 확인
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(()->new CustomException(HttpStatus.BAD_REQUEST, "유효하지 않은 재설정 링크입니다."));

        //토큰 만료 시간 확인
        if(resetToken.isExpired()){
            passwordResetTokenRepository.delete(resetToken);
            throw new CustomException(HttpStatus.UNAUTHORIZED, "재설정 링크의 유효시간이 지났습니다. 다시 요청해주세요");
        }

        //새 비밀번호 암호화해서 User 엔티티 업데이트
        User user = resetToken.getUser();
        String encodedPassword = bCryptPasswordEncoder.encode(newPassword);
        user.updatePassword(encodedPassword);

        //사용 완료된 토큰 삭제
        passwordResetTokenRepository.delete(resetToken);
    }

    //토큰 재발급
    public TokenDto reissueToken(String oldRefreshToken){
        //DB에서 프론트엔드가 보낸 Refresh Token이 존재하는지 확인
        RefreshToken tokenEntity = refreshTokenRepository.findByRefreshToken(oldRefreshToken)
                .orElseThrow(()->new CustomException(HttpStatus.UNAUTHORIZED,"유효하지 않거나 만료된 Refresh Token입니다."));

        //토큰의 유저 정보 찾기
        Long userId = tokenEntity.getUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(()->new CustomException(HttpStatus.NOT_FOUND,"회원 정보를 찾을 수 없습니다."));

        //새로운 토큰 생성
        String newAccessToken = jwtUtil.generateAccessToken(user.getId(), user.getEmail());
        String newRefreshToken = jwtUtil.generateRefreshToken(user.getId());

        refreshTokenRepository.delete(tokenEntity);

        //발급된 새로운 Refresh Token을 DB에 저장하고 최종 반환
        RefreshToken newTokenEntity = RefreshToken.builder()
                .userId(user.getId())
                .refreshToken(newRefreshToken)
                .expiredAt(LocalDateTime.now().plus(refreshTokenExpirationMs, ChronoUnit.MILLIS))
                .build();
        refreshTokenRepository.save(newTokenEntity);

        return TokenDto.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();
    }

}
