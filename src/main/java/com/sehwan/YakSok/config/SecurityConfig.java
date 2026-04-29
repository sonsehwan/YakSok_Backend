
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
                // 1. CSRF 보호 기능 비활성화
                .csrf(AbstractHttpConfigurer::disable)
                // 2. HTTP 기본 인증 비활성화
                .httpBasic(AbstractHttpConfigurer::disable)
                // 3. 경로별 권한 설정
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/users/signup").permitAll()
                        .requestMatchers("/api/users/login").permitAll()
                        .requestMatchers("/api/users/info").permitAll()
                        .requestMatchers("/api/users/password").permitAll()
                        .requestMatchers("/api/medicine/search").permitAll()
                        .requestMatchers("/api/users/**").permitAll()

                        .requestMatchers("/api/yaksok").permitAll()
                        .requestMatchers("/api/yaksok/**").permitAll()

                        .requestMatchers("/error").permitAll()

                        .anyRequest().authenticated()
                );

        return http.build();
    }
}