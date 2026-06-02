package com.sehwan.YakSok.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.DefaultUriBuilderFactory;

@Configuration
public class RestClientConfig {

    @Value("${api.medicine.url}")
    private String openApiUrl;

    @Value("${api.drugstore.url}")
    private String drugstoreApiUrl;

    @Bean
    public RestClient medicineRestClient() {
        // 공공데이터 API의 경우 이미 인코딩된 서비스 키가 다시 인코딩되는 것을 막기 위해 NONE 설정 유지
        DefaultUriBuilderFactory factory = new DefaultUriBuilderFactory(openApiUrl);
        factory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.NONE);

        return RestClient.builder()
                .uriBuilderFactory(factory)
                .baseUrl(openApiUrl)
                .build();
    }

    @Bean
    public RestClient drugstoreRestClient() {
        DefaultUriBuilderFactory factory = new DefaultUriBuilderFactory(drugstoreApiUrl);
        factory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.NONE);

        return RestClient.builder()
                .uriBuilderFactory(factory)
                .baseUrl(drugstoreApiUrl)
                .build();
    }

    @Bean
    public RestClient restClient() {
        return RestClient.builder().build();
    }
}