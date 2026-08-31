package com.sehwan.YakSok.fcm;

import com.google.firebase.messaging.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class FcmService {

    public List<String> sendData(List<String> targetTokens, Map<String, String> data){
        if (targetTokens == null || targetTokens.isEmpty()) {
            log.warn("전송할 FCM 토큰이 없습니다. (type: {})", data.get("type"));
            return List.of();
        }

        AndroidConfig androidConfig = AndroidConfig.builder()
                .setPriority(AndroidConfig.Priority.HIGH)
                .build();

        MulticastMessage message = MulticastMessage.builder()
                .addAllTokens(targetTokens)
                .setAndroidConfig(androidConfig)
                .putAllData(data)
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

    public List<String> sendMessage(
            List<String> targetTokens,
            String title,
            String body,
            String notificationId,
            FcmMessageType type
    ){
        return sendData(targetTokens, Map.of(
                "title", title,
                "body", body,
                "notification_id", notificationId,
                "type", type.name()
        ));
    }
}