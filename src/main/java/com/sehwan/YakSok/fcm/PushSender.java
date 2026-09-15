package com.sehwan.YakSok.fcm;

import com.sehwan.YakSok.user.entity.User;
import com.sehwan.YakSok.user.entity.UserDevice;
import com.sehwan.YakSok.user.repository.UserDeviceRepository;
import com.sehwan.YakSok.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class PushSender {

    private final UserDeviceRepository userDeviceRepository;
    private final UserRepository userRepository;
    private final FcmService fcmService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void sendData(Collection<Long> userIds, Map<String, String> data){
        if(userIds == null || userIds.isEmpty()) return;

        List<Long> notifiableUserIds = userRepository.findAllById(userIds).stream()
                .filter(User::isNotificationEnabled)
                .map(User::getId)
                .toList();

        if (notifiableUserIds.isEmpty()) return;

        List<String> tokens = userDeviceRepository.findAllByUserIdIn(notifiableUserIds)
                .stream()
                .map(UserDevice::getToken)
                .distinct()
                .toList();

        if (tokens.isEmpty()) {
            log.warn("등록된 기기가 없어 푸시를 보내지 못했습니다. userIds={}, type={}",
                    userIds, data.get("type"));
            return;
        }

        fcmService.sendData(tokens, data)
                .forEach(userDeviceRepository::deleteByToken);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void sendMessage(Collection<Long> userIds, String title, String body,
                            String notificationId, FcmMessageType type) {
        sendData(userIds, Map.of(
                "title", title,
                "body", body,
                "notificationId", notificationId,
                "type", type.name()
        ));
    }
}
