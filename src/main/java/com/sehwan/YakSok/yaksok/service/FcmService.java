package com.sehwan.YakSok.yaksok.service;

import com.google.firebase.messaging.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class FcmService {
    public List<String> sendMessage(
            List<String> targetTokens,
            String title,
            String body,
            String notificationId,
            FcmMessageType type
    ){
        if (targetTokens == null || targetTokens.isEmpty()) {
            log.warn("전송할 FCM 토큰이 없습니다. (title: {})", title);
            return List.of();  // 빈 리스트 반환
        }

        // 우선순위를 HIGH로 설정해야 화면이 꺼져있거나 앱이 백그라운드일 때도 즉각 알림이 수신됩니다.
        AndroidConfig androidConfig = AndroidConfig.builder()
                .setPriority(AndroidConfig.Priority.HIGH)
                .build();

        MulticastMessage message = MulticastMessage.builder()
                .addAllTokens(targetTokens)
                .setAndroidConfig(androidConfig) // 안드로이드 강제 깨우기 설정 추가
                .putData("title", title)
                .putData("body", body)
                .putData("notificationId", notificationId)
                .putData("type", type.name())
                .build();

        List<String> invalidTokens = new ArrayList<>();
        try{
            BatchResponse response = FirebaseMessaging.getInstance()
                    .sendEachForMulticast(message);
            List<SendResponse> results = response.getResponses();

            for(int i=0; i<results.size(); i++){
                SendResponse each = results.get(i);
                if(each.isSuccessful()) continue;

                MessagingErrorCode code = each.getException() != null ? each.getException().getMessagingErrorCode() : null;

                if(code == MessagingErrorCode.UNREGISTERED || code == MessagingErrorCode.INVALID_ARGUMENT){
                    invalidTokens.add(targetTokens.get(i));
                }
            }
            log.info("FCM 발송 완료. 성공 {} / 실패 {} / 정리 대상 {}",
                    response.getSuccessCount(), response.getFailureCount(), invalidTokens.size());
        }catch (FirebaseMessagingException e){
            log.error("FCM 알림 전송 실패. {}", e.getMessage(), e);
        }
        return invalidTokens;
    }
}