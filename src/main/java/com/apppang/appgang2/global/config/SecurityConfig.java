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
                        //테스트를 위해 경로가 /api/로 된 모든 주소 허락
                        .requestMatchers("/api/**").permitAll()
                        //그 외의 나머지 경로들은 모두 로그인해야 접근 가능
                        .anyRequest().authenticated()
                );
        return http.build();
    }
}
