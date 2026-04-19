package com.sehwan.YakSok.yaksok.service;

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

    @Transactional
    public SaveYaksokResponse saveYaksok(YaksokRequest request){
        String timeMorning = (request.isTakeMorning() && (request.getTimeMorning() == null ||  request.getTimeMorning().trim().isEmpty()) ? "오전 08:00" : request.getTimeMorning());
        String timeLunch = (request.isTakeLunch() && (request.getTimeLunch() == null ||  request.getTimeLunch().trim().isEmpty()) ? "오후 12:00" : request.getTimeLunch());
        String timeDinner = (request.isTakeDinner() && (request.getTimeDinner() == null ||  request.getTimeDinner().trim().isEmpty()) ? "오후 06:00" : request.getTimeDinner());

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

        List<Notification> notifications =  createNotification(savedYaksok);

        SaveYaksokResponse response = new SaveYaksokResponse(savedYaksok.getId(), notifications);

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
                Notification morningNoti = saveNotification(yaksok, dateStr, yaksok.getTimeMorning());
                notifications.add(morningNoti);
            }
            if(yaksok.isTakeLunch()){
                Notification lunchNoti = saveNotification(yaksok, dateStr, yaksok.getTimeLunch());
                notifications.add(lunchNoti);
            }
            if(yaksok.isTakeDinner()){
                Notification dinnerNoti = saveNotification(yaksok, dateStr, yaksok.getTimeDinner());
                notifications.add(dinnerNoti);
            }
        }
        return notifications;
    }

    private Notification saveNotification(Yaksok yaksok, String date, String time){
        System.out.println("이름: " + yaksok.getTitle());

        Notification notification = Notification.builder()
                .title(yaksok.getTitle())
                .date(date)
                .time(time)
                .instruction(yaksok.getDosageTime())
                .isTaken(false)
                .yaksok(yaksok)
                .build();
        notificationRepository.save(notification);

        return notification;
    }
}
