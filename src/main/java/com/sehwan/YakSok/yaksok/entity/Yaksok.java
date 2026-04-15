package com.sehwan.YakSok.yaksok.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Yaksok {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title; // 약속 이름

    @Column(nullable = false)
    private String startDate; // 복약 시작일

    @Column(nullable = false)
    private int prescriptionDays; // 복약일(이 값을 기준으로 종료일 계산)

    // 아침, 점심, 저녁 복용 여부
    private boolean takeMorning;
    private boolean takeLunch;
    private boolean takeDinner;

    @Column(nullable = false)
    private String dosageTime;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private YaksokStatus status = YaksokStatus.TAKING;

    @OneToMany(mappedBy = "yaksok", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default // 이 설정을 해두면 객체 생성할 때 값을 설정하지 않아도 자동으로 초기값으로 적용된다.
    private List<Pill> pills = new ArrayList<>();

    public void addPill(Pill pill){
        pills.add(pill);
        pill.setYaksok(this); //
    }
}
