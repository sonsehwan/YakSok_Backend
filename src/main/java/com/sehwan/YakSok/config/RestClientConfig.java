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
    //
    // read 타임아웃이 5초였는데, data.go.kr 게이트웨이가 평상시에도 응답에
    // 10초 이상 걸리는 경우가 있어(과부하 시 특히) 정상 요청까지 전부 타임아웃으로
    // 실패했다. connect는 짧게(막힌 경로를 빨리 포기) 유지하고 read만 넉넉히 준다.
    private ClientHttpRequestFactory timeoutRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(5));
        factory.setReadTimeout(Duration.ofSeconds(30));
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
        return RestClient.builder()
                .requestFactory(timeoutRequestFactory())
                .build();
    }
}