package com.apppang.appgang2.global.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtUtil {
    //application-secret.yml에 저장한 값 불러오기
    @Value("${jwt.access-token.expiration}")
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
                //TODO: UserDB 구현 완료 시 발급된 토큰을 DB에 저장하는 로직으로 변경 예정
                .setSubject(String.valueOf(userId)) //임시로 setSubject를 통해 사용자 식별
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis()+refreshTokenExpirationMs))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

}
