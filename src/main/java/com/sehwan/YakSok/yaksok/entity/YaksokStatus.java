package com.sehwan.YakSok.yaksok.entity;

import lombok.Getter;

@Getter
public enum YaksokStatus {
    TAKING("복약중"),
    COMPLETED("완료");

    private final String description;

    YaksokStatus(String description) {
        this.description = description;
    }
}
