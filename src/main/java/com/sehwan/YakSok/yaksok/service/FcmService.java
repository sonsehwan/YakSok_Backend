package com.sehwan.YakSok.yaksok.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class FcmService {
    public void sendMessage(String targetToken, String title, String body){
        if(targetToken == null || targetToken.trim().isEmpty()){
            log.warn("FCM 토큰이 비어 있어 알림을 전송할 수 없습니다.(title: {}", title);
            return;
        }

        Message message = Message.builder()
                .setToken(targetToken)
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                // 추가 데이터를 보내고 싶다면 putData 사용
                // .putData("customKey", "customValue")
                .build();

        try{
            String response = FirebaseMessaging.getInstance().send(message);
            log.info("FCM 알림 전송 성공! 메시지 ID : {}", response);
        }catch(FirebaseMessagingException e){
            log.error("FCM 알림 전송 실패: {}", e.getMessage(), e);
        }
    }
}
