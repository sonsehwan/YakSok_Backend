package com.sehwan.YakSok.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient webClient() {
        // 기본 설정을 포함한 WebClient 빈 생성
        return WebClient.builder()
                .build();
    }
}