package com.sehwan.YakSok.yaksok.event;

import com.sehwan.YakSok.fcm.FcmMessageType;
import com.sehwan.YakSok.fcm.PushSender;
import com.sehwan.YakSok.yaksok.repository.ShareYaksokRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Component
@Slf4j
@RequiredArgsConstructor
public class NotificationStatusChangedListener {

    private final ShareYaksokRepository shareYaksokRepository;
    private final PushSender pushSender;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(NotificationStatusChangedEvent e) {
        List<Long> watcherIds = shareYaksokRepository.findAllByYaksokId(e.yaksokId())
                .stream()
                .map(share -> share.getReceiver().getId())
                .distinct()
                .toList();

        // (1) 갱신 신호 - 소유자 + 공유자 모두. 알림은 뜨지 않고 화면만 다시 그린다.
        List<Long> everyone = Stream.concat(Stream.of(e.ownerId()), watcherIds.stream())
                .distinct()
                .toList();

        pushSender.sendData(everyone, Map.of("type", FcmMessageType.REFRESH.name()));

        // (2) 복약 완료 알림 - 공유자에게만, 완료했을 때만.
        if (e.isTaken() && !watcherIds.isEmpty()) {
            String title = "💊 " + e.ownerNickname() + "님의 복약 완료";
            String body = e.yaksokTitle() + "의 " + e.date().substring(5) + " "
                    + e.timeCategory() + " 복약을 완료했어요.";

            pushSender.sendMessage(watcherIds, title, body,
                    String.valueOf(e.notificationId()), FcmMessageType.TAKEN_ALERT);
        }
    }
}
