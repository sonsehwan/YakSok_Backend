package com.sehwan.YakSok.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1. CSRF 보호 기능 비활성화 (모바일 앱 통신 시 필수)
                .csrf(AbstractHttpConfigurer::disable)
                // 2. HTTP 기본 인증 비활성화
                .httpBasic(AbstractHttpConfigurer::disable)
                // 3. 경로별 권한 설정
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/users/signup").permitAll() // 회원가입 경로는 모두 허용
                        .requestMatchers("/api/users/login").permitAll()
                        .anyRequest().authenticated()                    // 그 외 요청은 인증 필요
                );

        return http.build();
    }
}