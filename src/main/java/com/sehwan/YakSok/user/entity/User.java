package com.sehwan.YakSok.user.entity;

import com.sehwan.YakSok.drugstore.entity.DrugStore;
import com.sehwan.YakSok.user.dto.response.UserResponse;
import com.sehwan.YakSok.yaksok.entity.Yaksok;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"social_type", "social_id"}),
        check = @CheckConstraint(
                name = "check_users_login_method",
                constraint = "(login_id IS NOT NULL AND password IS NOT NULL)"
                        + " OR (social_type IS NOT NULL AND social_id IS NOT NULL)"))
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

    // 카카오로 가입한 회원은 loginId·password가 null (33장). 둘 다 없으면 DB CHECK가 막는다
    @Column(unique = true, length = 20)
    private String loginId;

    @Column(length = 255)
    private String password;

    // 소셜 로그인 연동 정보. 일반 가입 회원은 null (연동 해제 = 둘 다 null로)
    @Column(name = "social_type", length = 20)
    private String socialType;

    @Column(name = "social_id", length = 100)
    private String socialId;

    @Column(unique = true, length = 50)
    private String nickname;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "drug_store_id")
    private DrugStore myDrugStore; // 일반 유저는 null

    // 6. 가입일자: 생성 시 자동 기록
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder.Default
    @Column(nullable = false, columnDefinition = "TINYINT(1) DEFAULT 1")
    private boolean notificationEnabled = true;

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
                .nickname(nickname)
                .role(role)
                .socialType(socialType)
                .myDrugStore(myDrugStore)
                .notificationEnabled(notificationEnabled)
                .build();
    }
}
