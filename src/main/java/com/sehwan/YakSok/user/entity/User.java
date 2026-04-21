package com.sehwan.YakSok.user.entity;

import com.sehwan.YakSok.yaksok.entity.Pill;
import com.sehwan.YakSok.yaksok.entity.Yaksok;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    // 1. 이메일: 기본키 (PK)
    @Id
    @Column(length = 100)
    private String email;

    // 3. 비밀번호: 필수
    @Column(nullable = false, length = 255)
    private String password;

    // 4. 선택 입력 사항
    @Column(unique = true, length = 50)
    private String nickname;

    @Column(length = 10)
    private String gender;

    private LocalDate birthdate;

    // 5. 알림 및 상태 관리 필드
    @Column(length = 255)
    private String fcmToken;

    @Builder.Default
    private Boolean penaltyEnable = false;

    @Builder.Default
    private Boolean isLocked = false;

    // 6. 가입일자: 생성 시 자동 기록
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default // 이 설정을 해두면 객체 생성할 때 값을 설정하지 않아도 자동으로 초기값으로 적용된다.
    private List<Yaksok> yaksoks = new ArrayList<>();

    public void addYaksok(Yaksok yaksok) {
        yaksoks.add(yaksok);
        yaksok.setUser(this);
    }
}
