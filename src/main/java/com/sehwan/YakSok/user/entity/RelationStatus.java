package com.sehwan.YakSok.user.entity;

public enum RelationStatus {
    SELF,      // 본인
    FRIEND,    // 이미 친구
    SENT,      // 내가 요청을 보내 대기 중
    RECEIVED,  // 상대가 나에게 요청을 보내 대기 중
    NONE       // 아무 관계 없음
}
