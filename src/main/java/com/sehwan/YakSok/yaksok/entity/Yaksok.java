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
    private Long num;

    @Column(nullable = false)
    private String name;

    private String date;

    @OneToMany(mappedBy = "yaksok", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Pill> pills = new ArrayList<>();

    public void addPill(Pill pill){
        pills.add(pill);
    }
}
