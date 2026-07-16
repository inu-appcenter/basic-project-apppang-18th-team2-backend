package com.apppang.apppang2.global.security.filter;

import com.apppang.apppang2.global.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j  //log 사용
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter { //요청당 한번의 실행을 보장

    private static final String BEARER_PREFIX = "Bearer ";      //토큰 가지고 있는 사람(Bearer)에게 권한 인정
    private final JwtUtil jwtUtil;

    @Override
    //필터는 오직 스프링/서블릿 컨테이너 시스템에 의해서만 호출되어야하기 때문에 protected
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException{
        //클라이언트가 보낸 요청 정보(헤더,바디,URI), 서버가 클라이언트에게 보낼 응답 정보(쿠키,헤더설정), 다음 필터로 갈 수 있게 연결해주는 통로

        //헤더에서 토큰 추출
        String token = resolveToken(request);

        //토큰이 비어있지 않는지 유효한지 검사
        if(StringUtils.hasText(token) && jwtUtil.validateToken(token)){

            //토큰 안에 있는 유저 정보를 스프링 전용 신분증으로 변환
            Authentication auth = jwtUtil.getAuthentication(token);

            //변환한 신분증을 스프링 시큐리티의 보안 저장소(SecurityContext)에 등록
            SecurityContextHolder.getContext().setAuthentication(auth);

            //잘 등록되었다고 로그에 기록
            log.debug("Security Context에 '{}' 인증정보를 저장했습니다, uri: {}", auth.getName(), request.getRequestURI());
        }else{
            //토큰이 없거나 가짜라면 로그만 남기고 등록은 안함
            log.debug("유효한 JWT 토큰이 없습니다, uri: {}",request.getRequestURI());
        }

        //스프링 시큐리티는 여러 필터들이 줄 세워져 있어 다음 필터로 이동
        filterChain.doFilter(request, response);
    }

    //HTTP 요청에서 Bearer 은 잘라내고 토큰만 추출하는 메서드
    private String resolveToken(HttpServletRequest request){
        //Authorization 헤더에서 값 읽어오기
        String bearerToken = request.getHeader(HttpHeaders.AUTHORIZATION);

        //"Bearer "로 시작하는 JWT 토큰인지 검사 후 제거 후 반환
        if(StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)){
            return bearerToken.substring(BEARER_PREFIX.length());    //"Bearer " 제외하고 토큰만 추출
        }
        return null;
    }


}
