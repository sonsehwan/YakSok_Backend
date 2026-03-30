package com.sehwan.YakSok;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


/**
 * @SpringBootApplication은 아래 3가지 핵심 기능을 합친 것입니다.
 *
 * @SpringBootConfiguration: 이 클래스가 스프링 설정을 담고 있음을 알립니다.
 *
 * @ComponentScan: 해당 프로젝트 패키지 하위의 @Service, @Repository 등을 찾아 빈으로 등록합니다.
 *
 * @EnableAutoConfiguration: 가장 중요한 부분으로, 외부 라이브러리에 미리 정의된 자동 설정들을 활성화합니다.
 **/
@SpringBootApplication
public class YakSokApplication {
    public static void main(String[] args) {
        SpringApplication.run(YakSokApplication.class, args);
    }
}
