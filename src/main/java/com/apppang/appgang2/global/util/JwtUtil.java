package com.apppang.appgang2.global.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {
    private Key key; //위의 문자열을 암호화 알고리즘이 쓸 수 있는 Key 객체로 변환해 저장할 변수
    private static final long accessTokenExpirationMs = 60*60*1000; //60분
    private static final long refreshTokenExpirationMs = 60*60*1000*24*14; //14일

    //application-secret.yml에서 비밀키 읽기
    public JwtUtil(@Value("${jwt.secret}")String secretKey){
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
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
    public String generateRefreshToken(){
        return Jwts.builder()
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis()+refreshTokenExpirationMs))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

}
