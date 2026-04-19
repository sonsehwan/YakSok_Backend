package com.sehwan.YakSok.yaksok.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Pill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String image;

    @Column(nullable = false)
    private int dailyFrequency; // 1일 투여 횟수

    @Column(nullable = false)
    private String dosage; // 1회 투약량

    @JsonIgnore // JSON 변환시 무한 참조 방지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE) // DB에서도 실제로 CASCADE설정을 해준다.
    private Yaksok yaksok;
}
