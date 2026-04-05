package com.sehwan.YakSok.user.dto;

public class MedicineSearchRequest {
    private String keyword;

    public MedicineSearchRequest(String keyword) {
        this.keyword = keyword;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }
}
