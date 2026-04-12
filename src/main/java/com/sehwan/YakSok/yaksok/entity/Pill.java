package com.sehwan.YakSok.yaksok.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "pill")
public class Pill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "image", nullable = false)
    private String image;

    @Column(name = "day_frequency", nullable = false)
    private int dayFrequency;

    @Column(name = "duration", nullable = false)
    private int duration;

    @Column(name = "day_dosage", nullable = false)
    private int dayDosage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "yaksok_num", nullable = false)
    private Yaksok yaksok;
}
