package com.sehwan.YakSok.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.time.Duration;

@Configuration
public class RestClientConfig {

    @Value("${api.medicine.url}")
    private String openApiUrl;

    @Value("${api.drugstore.url}")
    private String drugstoreApiUrl;

    // 공공데이터 API가 응답하지 않을 때 요청 스레드가 무한정 묶이는 것을 막는다.
    // 타임아웃을 두지 않으면 외부 API 장애가 서버 전체 장애로 번진다.
    private ClientHttpRequestFactory timeoutRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(3));
        factory.setReadTimeout(Duration.ofSeconds(5));
        return factory;
    }

    @Bean
    public RestClient medicineRestClient() {
        // 공공데이터 API의 경우 이미 인코딩된 서비스 키가 다시 인코딩되는 것을 막기 위해 NONE 설정 유지
        DefaultUriBuilderFactory factory = new DefaultUriBuilderFactory(openApiUrl);
        factory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.NONE);

        return RestClient.builder()
                .uriBuilderFactory(factory)
                .baseUrl(openApiUrl)
                .requestFactory(timeoutRequestFactory())
                .build();
    }

    @Bean
    public RestClient drugstoreRestClient() {
        DefaultUriBuilderFactory factory = new DefaultUriBuilderFactory(drugstoreApiUrl);
        factory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.NONE);

        return RestClient.builder()
                .uriBuilderFactory(factory)
                .baseUrl(drugstoreApiUrl)
                .requestFactory(timeoutRequestFactory())
                .build();
    }

    @Bean
    public RestClient restClient() {
        return RestClient.builder().build();
    }
}