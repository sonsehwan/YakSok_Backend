package com.sehwan.YakSok.yaksok.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String date;

    @Column(nullable = false)
    private String time;

    @Column(nullable = false)
    private String instruction; // 복욕 정보 (ex: 식후, 식전 등등)

    @Column(nullable = false)
    private boolean isTaken; //

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Yaksok yaksok; // 왜래키 연결
}
