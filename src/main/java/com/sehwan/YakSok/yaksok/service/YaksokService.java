package com.sehwan.YakSok.yaksok.service;

import com.sehwan.YakSok.user.entity.User;
import com.sehwan.YakSok.user.repository.UserRepository;
import com.sehwan.YakSok.yaksok.dto.PillRequest;
import com.sehwan.YakSok.yaksok.dto.SaveYaksokResponse;
import com.sehwan.YakSok.yaksok.dto.YaksokRequest;
import com.sehwan.YakSok.yaksok.entity.Notification;
import com.sehwan.YakSok.yaksok.entity.Pill;
import com.sehwan.YakSok.yaksok.entity.Yaksok;
import com.sehwan.YakSok.yaksok.repository.NotificationRepository;
import com.sehwan.YakSok.yaksok.repository.PillRepository;
import com.sehwan.YakSok.yaksok.repository.YaksokRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class YaksokService {

    private final PillRepository pillRepository;
    private final YaksokRepository yaksokRepository;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Transactional
    public SaveYaksokResponse saveYaksok(YaksokRequest request){
        String timeMorning = (request.isTakeMorning() && (request.getTimeMorning() == null ||  request.getTimeMorning().trim().isEmpty()) ? "오전 08:00" : request.getTimeMorning());
        String timeLunch = (request.isTakeLunch() && (request.getTimeLunch() == null ||  request.getTimeLunch().trim().isEmpty()) ? "오후 12:00" : request.getTimeLunch());
        String timeDinner = (request.isTakeDinner() && (request.getTimeDinner() == null ||  request.getTimeDinner().trim().isEmpty()) ? "오후 06:00" : request.getTimeDinner());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(()-> new RuntimeException("사용자를 찾을 수 없습니다."));

        Yaksok yaksok = Yaksok.builder()
                .title(request.getTitle())
                .startDate(request.getStartDate())
                .prescriptionDays(request.getPrescriptionDays())
                .takeMorning(request.isTakeMorning())
                .takeLunch(request.isTakeLunch())
                .takeDinner(request.isTakeDinner())
                .timeMorning(timeMorning)
                .timeLunch(timeLunch)
                .timeDinner(timeDinner)
                .dosageTime(request.getDosageTime())
                .build();
        user.addYaksok(yaksok);

        if(request.getPills() != null){
            for(PillRequest pillRequest : request.getPills()){
                Pill pill = Pill.builder()
                        .name(pillRequest.getName())
                        .image(pillRequest.getImage())
                        .dailyFrequency(pillRequest.getDailyFrequency())
                        .dosage(pillRequest.getDosage())
                        .build();
                yaksok.addPill(pill);
            }
        }

        Yaksok savedYaksok = yaksokRepository.save(yaksok);

        createNotification(savedYaksok);
        List<Notification> notifications = notificationRepository.findAllByOrderByTimeAscTitleAsc();

                SaveYaksokResponse response = new SaveYaksokResponse(savedYaksok, notifications);

        return response;
    }

    private List<Notification> createNotification(Yaksok yaksok){
        LocalDate stasrtDate = LocalDate.parse(yaksok.getStartDate());
        int days = yaksok.getPrescriptionDays();

        List<Notification> notifications = new ArrayList<>();

        for(int i=0; i<days; i++){
            LocalDate currentDate = stasrtDate.plusDays(i);
            String dateStr = currentDate.toString();

            if(yaksok.isTakeMorning()){
                Notification morningNoti = saveNotification(yaksok,"아침", dateStr, yaksok.getTimeMorning());
                notifications.add(morningNoti);
            }
            if(yaksok.isTakeLunch()){
                Notification lunchNoti = saveNotification(yaksok, "점심", dateStr, yaksok.getTimeLunch());
                notifications.add(lunchNoti);
            }
            if(yaksok.isTakeDinner()){
                Notification dinnerNoti = saveNotification(yaksok, "저녁", dateStr, yaksok.getTimeDinner());
                notifications.add(dinnerNoti);
            }
        }
        return notifications;
    }

    private Notification saveNotification(Yaksok yaksok, String timeCatagory, String date, String time){
        System.out.println("이름: " + yaksok.getTitle());

        Notification notification = Notification.builder()
                .title(yaksok.getTitle())
                .timeCategory(timeCatagory)
                .date(date)
                .time(time)
                .instruction(yaksok.getDosageTime())
                .isTaken(false)
                .yaksok(yaksok)
                .build();
        notificationRepository.save(notification);

        return notification;
    }

    public void updateNotificationStatus(Long notificationId, boolean isTaken) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("해당 알림을 찾을 수 없습니다. id=" + notificationId));

        // Entity에 맞게 메서드명 수정 필요 (예: updateIsTaken 또는 Setter 사용)
        notification.setTaken(isTaken);
    }

    public List<Notification> findAllByUserEmail(@Param("email") String email) {
        return notificationRepository.findAllByUserEmail(email);
    }

    public List<Yaksok> getYaksokListByUserEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        return yaksokRepository.findAllByUserEmail(email);
    }

    @Transactional
    public void deleteYaksok(Long id) {
        Yaksok yaksok = yaksokRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 약속이 존재하지 않습니다. id=" + id));

        // JPA의 Cascade 설정 덕분에 yaksok을 삭제하면 연관된 Pill, Notification도 자동 삭제됩니다.
        yaksokRepository.delete(yaksok);
    }
}
