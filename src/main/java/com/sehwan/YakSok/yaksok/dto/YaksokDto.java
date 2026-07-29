package com.sehwan.YakSok.yaksok.dto;

import com.sehwan.YakSok.user.entity.User;
import com.sehwan.YakSok.yaksok.entity.Notification;
import com.sehwan.YakSok.yaksok.entity.Pill;
import com.sehwan.YakSok.yaksok.entity.Yaksok;
import com.sehwan.YakSok.yaksok.entity.YaksokStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class YaksokDto {
    private Long id;
    private String title;
    private String startDate;
    private int prescriptionDays;

    // 아침, 점심, 저녁 복용 여부
    private boolean takeMorning;
    private boolean takeLunch;
    private boolean takeDinner;

    // 알림 시간
    private String timeMorning;
    private String timeLunch;
    private String timeDinner;

    // 복용 타이밍(식전 30분, 식후 30분, 식후)
    private String dosageTime;

    // 복약 완료 상태
    private YaksokStatus status;

    private List<PillDto> pills;

    private int totalNotifications;
    private int currentClearNotifications;

    // 공유받은 사람에게 누구의 약속인지 알려주기 위한 값.
    // User 엔티티를 그대로 담으면 비밀번호와 FCM 토큰까지 응답에 실린다.
    private String ownerNickname;

    public static YaksokDto from(Yaksok yaksok) {
        return new YaksokDto(
                yaksok.getId(),
                yaksok.getTitle(),
                yaksok.getStartDate(),
                yaksok.getPrescriptionDays(),
                yaksok.isTakeMorning(),
                yaksok.isTakeLunch(),
                yaksok.isTakeDinner(),
                yaksok.getTimeMorning(),
                yaksok.getTimeLunch(),
                yaksok.getTimeDinner(),
                yaksok.getDosageTime(),
                yaksok.getStatus(),
                yaksok.getPills().stream().map(PillDto::from).toList(),
                yaksok.getTotalNotifications(),
                yaksok.getCurrentClearNotifications(),
                yaksok.getUser().getNickname()
        );
    }
}