package com.sehwan.YakSok.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;

@Slf4j
@Configuration
public class FirebaseConfig {

    @PostConstruct
    public void init() {
        try{
            if(FirebaseApp.getApps().isEmpty()) {
                ClassPathResource resource = new ClassPathResource("firebase-service-account.json");
                InputStream serviceAccount = resource.getInputStream();

                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .build();

                FirebaseApp.initializeApp(options);
                log.info("FirebaseApp이 성공적으로 초기화되었습니다.");
            }
        }catch(Exception e){
            log.error("FirebaseApp 초기화 실패: {}", e.getMessage(), e);
        }
    }
}
