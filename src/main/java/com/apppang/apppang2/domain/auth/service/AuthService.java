package com.apppang.apppang2.domain.auth.service;

import com.apppang.apppang2.domain.auth.dto.request.FindIdRequest;
import com.apppang.apppang2.domain.auth.dto.response.FindIdResponse;
import com.apppang.apppang2.domain.auth.dto.request.LoginRequest;
import com.apppang.apppang2.domain.auth.dto.response.LoginResponse;
import com.apppang.apppang2.domain.auth.dto.request.SignupRequest;
import com.apppang.apppang2.domain.user.entity.Role;
import com.apppang.apppang2.domain.user.entity.User;
import com.apppang.apppang2.domain.user.repository.UserRepository;
import com.apppang.apppang2.global.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@RequiredArgsConstructor
@Service
public class AuthService {
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;


    //컨트롤러의 최종 응답을 위해 DB에 저장된 후 발급된 userId(Long)를 반환
    @Transactional
    public Long signup(SignupRequest signupRequest) {
        String email = signupRequest.getEmail();
        String password = signupRequest.getPassword();

        //이메일 중복 검사
        Boolean isExist = userRepository.existsByEmail(email);

        //중복일 경우 예외 처리
        if (isExist) {
            throw new IllegalArgumentException("이미 사용중인 이메일입니다.");
        }

        //받은 DTO를 User 엔티티 형태로 옮겨담는 과정
        User user = User.builder()
                .email(email)
                .password(bCryptPasswordEncoder.encode(password))   //비밀번호 암호화
                .name(signupRequest.getName())
                .phone(signupRequest.getPhone())
                .agreeRequiredTerms(signupRequest.isAgreeRequiredTerms())
                .agreeMarketing(signupRequest.isAgreeMarketing())
                .role(Role.USER) //swagger 테스트를 위해 role를 추가
                .build();

        //엔티티를 실제 DB에 저장
        User savedUser = userRepository.save(user);

        //저장 후 DB에서 발급된 기본키(유저아이디)를 꺼내서 컨트롤러로 반환
        return savedUser.getId();
    }

    public LoginResponse login(LoginRequest request) {

        //사용자 확인 (없으면 예외 발생)
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("가입되지 않은 이메일입니다."));

        //비밀번호 검증 (암호화된 비번과 비교)
        if (!bCryptPasswordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        //토큰 생성
        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getEmail());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());

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
        return !userRepository.existsByEmail(email);
    }

    //아이디 찾기
    public FindIdResponse findId(FindIdRequest request){
        //이름과 휴대폰번호로 유저 엔티티 조회
        User user = userRepository.findByNameAndPhone(request.getName(),request.getPhone())
                .orElseThrow(()->new IllegalArgumentException( "일치하는 회원 정보를 찾을 수 없습니다."));

        //조회된 유저의 이메일 반환
        return new FindIdResponse(user.getEmail());
    }

}
