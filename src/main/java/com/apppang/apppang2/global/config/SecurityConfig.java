package com.apppang.apppang2.global.config;

import com.apppang.apppang2.global.security.CustomAuthenticationEntryPoint;
import com.apppang.apppang2.global.security.filter.JwtAuthenticationFilter;
import com.apppang.apppang2.global.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtUtil jwtUtil;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    //비밀번호 암호화
    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder(){
        return new BCryptPasswordEncoder();
    }

    // 💡 1. Swagger 관련 경로들이 시큐리티 필터 체인(JWT 필터 포함)을 아예 타지 않도록 완전 예외 처리
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring()
                .requestMatchers(
                        "/api-docs/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/swagger-resources/**",
                        "/webjars/**"
                );
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
        http
                .cors(cors->cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .formLogin(form -> form.disable())
                .httpBasic(basic->basic.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))   //세션 사용안함
                //비로그인 상태로 인증 필요 경로 접근 시 401 + 공통 JSON 응답 (미등록 시 기본값인 403 빈 응답이 나감)
                .exceptionHandling(ex -> ex.authenticationEntryPoint(customAuthenticationEntryPoint))
                //필터 추가
                .addFilterBefore(new JwtAuthenticationFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class)
                //요청 권한 설정
                .authorizeHttpRequests(auth -> auth
                        //테스트를 위해 경로가 /api/로 된 모든 주소 허락
                        .requestMatchers(
                                "/api/auth/**",
                                "/api/banners",
                                "/api/products/**"
                        ).permitAll()
                        //그 외의 나머지 경로들은 모두 로그인해야 접근 가능
                        .anyRequest().authenticated()
                );
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(){
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowCredentials(true);   //프론트에서 인증정보를 포함해서 요청할 수 있도록 허용
        //프론트엔드 서버 주소
        config.setAllowedOrigins(List.of(
                "http://localhost:5173"
        ));

        //허용할 HTTP 메서드(OPTIONS는 Preflight 요청을 위해 필수)
        config.setAllowedMethods((List.of("GET","POST","PUT","DELETE","PATCH","OPTIONS")));

        //허용할 헤더
        config.setAllowedHeaders(List.of("*"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        //모든 API 경로에 대해 위 CORS 설정을 적용
        source.registerCorsConfiguration("/**",config);
        return source;
    }
}
