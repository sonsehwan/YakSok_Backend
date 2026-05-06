package com.sehwan.YakSok.yaksok.service;

import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.AndroidNotification;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class FcmService {
    public void sendMessage(String targetToken, String title, String body, String notificationId){
        if(targetToken == null || targetToken.trim().isEmpty()){
            log.warn("FCM 토큰이 비어 있어 알림을 전송할 수 없습니다.(title: {})", title);
            return;
        }

        // 우선순위를 HIGH로 설정해야 화면이 꺼져있거나 앱이 백그라운드일 때도 즉각 알림이 수신됩니다.
        AndroidConfig androidConfig = AndroidConfig.builder()
                .setPriority(AndroidConfig.Priority.HIGH)
                .build();

        Message message = Message.builder()
                .setToken(targetToken)
                .setAndroidConfig(androidConfig) // 안드로이드 강제 깨우기 설정 추가
                .putData("title", title)
                .putData("body", body)
                .putData("notificationId", notificationId)
                .build();

        try{
            String response = FirebaseMessaging.getInstance().send(message);
            log.info("FCM 알림 전송 성공! 메시지 ID : {}", response);
        }catch(FirebaseMessagingException e){
            log.error("FCM 알림 전송 실패: {}", e.getMessage(), e);
        }
    }
}