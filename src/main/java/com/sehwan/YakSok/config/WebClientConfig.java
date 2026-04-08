package com.sehwan.YakSok.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.DefaultUriBuilderFactory;

@Configuration
public class WebClientConfig {

    @Value("${api.medicine.url}")
    private String openApiUrl;

    @Bean
    public WebClient medicineWebClient() {

        DefaultUriBuilderFactory factory = new DefaultUriBuilderFactory(openApiUrl);
        factory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.NONE);

        return WebClient.builder()
                .uriBuilderFactory(factory)
                .baseUrl(openApiUrl)
                .build();
    }

    @Bean
    public WebClient webClient() {
        return WebClient.builder().build();
    }
}