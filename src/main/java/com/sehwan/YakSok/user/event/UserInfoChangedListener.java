package com.sehwan.YakSok.user.event;

import com.sehwan.YakSok.fcm.FcmMessageType;
import com.sehwan.YakSok.fcm.PushSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class UserInfoChangedListener {

    private final PushSender pushSender;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(UserInfoChangedEvent e) {
        try {
            pushSender.sendData(List.of(e.userId()), Map.of("type", FcmMessageType.USER_REFRESH.name()));
        } catch (Exception ex) {
            log.warn("내 정보 갱신 푸시 실패 userId={}", e.userId(), ex);
        }
    }
}
