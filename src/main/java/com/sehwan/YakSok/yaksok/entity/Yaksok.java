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
@Table(name = "yaksok")
public class Yaksok {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "num")
    private Long num;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "date")
    private String date;

    @OneToMany(mappedBy = "yaksok", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Pill> pills = new ArrayList<>();

    public void addPill(Pill pill){
        pills.add(pill);
    }
}
