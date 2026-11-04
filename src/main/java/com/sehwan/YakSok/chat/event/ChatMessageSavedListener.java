package com.sehwan.YakSok.chat.event;

import com.sehwan.YakSok.chat.entity.ChatParticipant;
import com.sehwan.YakSok.chat.repository.ChatParticipantRepository;
import com.sehwan.YakSok.fcm.FcmMessageType;
import com.sehwan.YakSok.fcm.PushSender;
import com.sehwan.YakSok.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Component
@Slf4j
@RequiredArgsConstructor
public class ChatMessageSavedListener {

    private final ChatParticipantRepository chatParticipantRepository;
    private final UserRepository userRepository;
    private final PushSender pushSender;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(ChatMessageSavedEvent e){
        try{
            Set<Long> recipients = new HashSet<>();
            for (ChatParticipant p :
            chatParticipantRepository.findAllWithRefsByRoomId(e.roomId())){
                if(p.getId().equals(e.senderParticipantId())) continue;
                if(p.isUser()){
                    recipients.add(p.getUser().getId());
                }else{
                    userRepository.findAllByMyDrugStore_Hpid(p.getDrugStore().getHpid())
                            .forEach(user -> recipients.add(user.getId()));
                }
            }

            pushSender.sendData(recipients, Map.of(
                    "type", FcmMessageType.CHAT.name(),
                    "title", e.senderName(),
                    "body", e.preview(),
                    "notificationId", String.valueOf(e.roomId()),
                    "roomId", String.valueOf(e.roomId())));
        } catch (Exception ex) {
            log.warn("채팅 푸시 실패 roomId = {}", e.roomId(), ex);
        }
    }
}
