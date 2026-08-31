package com.sehwan.YakSok.yaksok.scheduler;

import com.sehwan.YakSok.user.entity.User;
import com.sehwan.YakSok.user.entity.UserDevice;
import com.sehwan.YakSok.user.repository.UserDeviceRepository;
import com.sehwan.YakSok.yaksok.entity.Notification;
import com.sehwan.YakSok.yaksok.repository.NotificationRepository;
import com.sehwan.YakSok.yaksok.service.FcmMessageType;
import com.sehwan.YakSok.yaksok.service.FcmService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationScheduler {
    private final FcmService fcmService;
    private final NotificationRepository notificationRepository;
    private final UserDeviceRepository userDeviceRepository;

    @Scheduled(cron = "0 * * * * *")
    @Transactional
    public void sendScheduelNotifications(){
        ZoneId kst = ZoneId.of("Asia/Seoul");

        LocalDate today = LocalDate.now(kst);
        String stringToday = today.toString();

        DateTimeFormatter now = DateTimeFormatter.ofPattern("a hh:mm", Locale.KOREAN);
        String stringNow = LocalTime.now(kst).format(now);

        log.info("알림 스케줄러 실행 중... 현재 시간:ㅣ {} {}", stringToday, stringNow);

        try{
            List<Notification> notifications = notificationRepository.findByDateAndTimeAndIsTakenFalse(stringToday, stringNow);

            if(notifications.isEmpty()){
                log.info("현재 시간에 보낼 알림이 없습니다.");
                return;
            }

            for(Notification noti : notifications){
                User user = noti.getYaksok().getUser();
                List<String> tokens = userDeviceRepository.findAllByUserId(user.getId())
                        .stream().map(UserDevice::getToken).toList();

                String title = "💊 약 복용 시간입니다!";
                String body = noti.getTitle() + "을 지킬 시간이에요.";

                if(tokens.isEmpty()){
                    log.warn("유저 {} 의 등록된 기기가 없어 알림을 보내지 못했습니다.", user.getEmail());
                    continue;
                }

                List<String> invalid = fcmService.sendMessage(
                        tokens, title, body, String.valueOf(noti.getId()), FcmMessageType.REMINDER);
                invalid.forEach(userDeviceRepository::deleteByToken);
            }
        } catch (Exception e) {
            log.error("알림 스케줄러 실행 중 오류 발생: {}", e.getMessage(), e);
        }
    }

}
