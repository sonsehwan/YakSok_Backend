package com.sehwan.YakSok.user.event;

import com.sehwan.YakSok.fcm.FcmMessageType;
import com.sehwan.YakSok.fcm.PushSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class FriendshipChangedListener {

    private final PushSender pushSender;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(FriendshipChangedEvent e) {
        try {
            boolean requested = e.kind() == FriendshipChangedEvent.Kind.REQUESTED;
            String title = requested ? "친구 요청" : "친구 수락";
            String body = requested
                    ? e.actorNickname() + "님이 친구 요청을 보냈어요."
                    : e.actorNickname() + "님과 친구가 되었어요.";

            pushSender.sendMessage(List.of(e.targetUserId()), title, body,
                    String.valueOf(e.actorUserId()), FcmMessageType.FRIENDSHIP);
        } catch (Exception ex) {
            log.warn("친구 푸시 실패 targetUserId={}", e.targetUserId(), ex);
        }
    }
}
