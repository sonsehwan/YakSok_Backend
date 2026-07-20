package com.sehwan.YakSok.user.entity;

import com.sehwan.YakSok.drugstore.entity.DrugStore;
import com.sehwan.YakSok.user.dto.response.UserResponse;
import com.sehwan.YakSok.yaksok.entity.Yaksok;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100, unique = true)
    private String email;

    // 3. 비밀번호: 필수
    @Column(nullable = false, length = 255)
    private String password;

    @Column(unique = true, length = 50)
    private String nickname;

    @Column(length = 10)
    private String gender;

    private LocalDate birthdate;

    // 5. 알림 및 상태 관리 필드
    @Column(length = 255)
    private String fcmToken;

    //벌금 여부
    @Builder.Default
    private Boolean penaltyEnable = false;

    @Builder.Default
    private Boolean isLocked = false;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "drug_store_id")
    private DrugStore myDrugStore; // 일반 유저는 null

    // 6. 가입일자: 생성 시 자동 기록
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @Builder.Default // 이 설정을 해두면 객체 생성할 때 값을 설정하지 않아도 자동으로 초기값으로 적용된다.
    private List<Yaksok> yaksoks = new ArrayList<>();

    public void addYaksok(Yaksok yaksok) {
        yaksoks.add(yaksok);
        yaksok.setUser(this);
    }

    public UserResponse toDto() {
        return UserResponse.builder()
                .id(id)
                .email(email)
                .password(password)
                .nickname(nickname)
                .gender(gender)
                .birthdate(birthdate)
                .fcmToken(fcmToken)
                .role(role)
                .penaltyEnable(penaltyEnable) // 초기 가입 시 기본값
                .isLocked(isLocked)      // 초기 가입 시 기본값
                .myDrugStore(myDrugStore)
                .build();
    }
}
