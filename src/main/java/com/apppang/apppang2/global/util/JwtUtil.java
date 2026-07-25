package com.apppang.apppang2.global.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;


import java.security.Key;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;

@Component
public class JwtUtil {
    //토큰 생성, 내용 검증, 유저 정보 추출

    @Value("${jwt.access-token.expiration}")    //application-secret.yml에 저장한 값 불러오기
    private long accessTokenExpirationMs;

    @Value("${jwt.refresh-token.expiration}")
    private long refreshTokenExpirationMs;

    @Value("${jwt.secret}")
    private String secretKey;

    private Key key;    //위의 문자열을 암호화 알고리즘이 쓸 수 있는 Key 객체로 변환해 저장할 변수

    //모든 의존성 주입이 이루어진 후 수행
    @PostConstruct
    public void init(){
        //보안을 위해 시크릿 키는 base 64로 인코딩된 상태이므로 사용 전 디코딩 과정
        byte[] keyBytes = Base64.getDecoder().decode(secretKey);
        //jjwt 라이브러리가 서명에 사용할 수 있도록 HMAC-SHA 알고리즘 기반의 Key 객체로 변환
        key = Keys.hmacShaKeyFor(keyBytes);
    }

    //accessToken 생성
    public String generateAccessToken(Long userId, String email){
        Date date = new Date(); //현재 시간을 가져와 JWT의 발급시간과 만료 시간을 설정
        return Jwts.builder()
                .setSubject(String.valueOf(userId)) //고유 PK를 토큰의 주체를 식별하는 subject 클레임에 저장
                .claim("email",email)
                .setExpiration(new Date(date.getTime()+accessTokenExpirationMs))
                .setIssuedAt(date)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    //refreshToken 생성
    public String generateRefreshToken(Long userId){
        return Jwts.builder()
                .setSubject(String.valueOf(userId)) //유저ID로 토큰 주인 식별
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis()+refreshTokenExpirationMs))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    //토큰이 변조되지 않았고 만료되지 않았는지 검증
    public boolean validateToken(String token){
        try{
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token); //토큰 파싱 시도
            return true;
        } catch(io.jsonwebtoken.security.SecurityException | MalformedJwtException e){
            //잘못된 JWT 서명이나 형식일 때
        } catch(ExpiredJwtException e){
            //토큰이 만료되었을 때
        } catch(UnsupportedJwtException e){
            //지원되지 않는 JWT 형식일 때
        } catch(IllegalArgumentException e){
            //토큰 내용이 비어있을 때
        }
        return false;
    }

    //토큰에서 내부에 저장된 데이터 추출
    public Claims getClaims(String token){
        return Jwts.parserBuilder()                 //JWT를 해독할 파서 생성
                .setSigningKey(key)                 //서버가 가지고 있는 서명키 주입
                .build()                            //주입된 키를 바탕으로 파서 객체 생성
                .parseClaimsJws(token)              //입력된 토큰의 서명을 검증하고 해독하여 JWT 객체로 반환
                .getBody();                         //해독에 성공한 객체에서 페이로드 영역에 해당하느 Claims 꺼내서 반환
    }

    //필터에서 유효한 토큰을 받았을 때 토큰을 기반으로 인증 객체 생성
    public Authentication getAuthentication(String token){
        //DB 조회없이 토큰에서 뽑은 userId만 SecurityContext에 넣도록 최적화
        Claims claims = getClaims(token);

        //토큰 생성 시 Subject에 담았던 userId(String)를 꺼내서 Long으로 변환
        Long userId = Long.valueOf(claims.getSubject());

        //변환한 userId를 스프링 시큐리티 신분증으로 등록
        return new UsernamePasswordAuthenticationToken(userId, "", Collections.emptyList());

    }
}
