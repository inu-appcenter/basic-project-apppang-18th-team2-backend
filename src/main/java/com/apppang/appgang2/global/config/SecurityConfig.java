package com.apppang.appgang2.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    /* DB 연동 후 로직 수정
    *  JWT 필터 추가 예정
    * */

    //비밀번호 암호화
    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder(){
        return new BCryptPasswordEncoder();
    }


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
        http
                .csrf(csrf -> csrf.disable())

                //요청 권한 설정
                .authorizeHttpRequests(auth -> auth
                        //모든 회원가입과 로그인은 허락
                        .requestMatchers("/api/auth/signup","/api/auth/login","/error").permitAll()
                        //그 외의 나머지 경로들은 모두 로그인해야 접근 가능
                        .anyRequest().authenticated()
                );
        return http.build();
    }
}
