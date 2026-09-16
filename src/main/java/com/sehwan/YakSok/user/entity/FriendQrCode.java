package com.sehwan.YakSok.user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Entity
@Table(name = "friend_qr_code")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FriendQrCode {

    private static final int TTL_MINUTES = 5;

    // Spring Boot 4.0.3(Hibernate 7 계열)에서 @MapsId 파생 식별자를 쓰면 리포지토리 빈을
    // 초기화하는 단계에서 hasIdClass() 오판으로 죽는 프레임워크 버그가 있어, 파생 식별자 대신
    // 명시적 스칼라 PK + 읽기전용 @JoinColumn(같은 물리 컬럼을 공유)으로 우회한다.
    @Id
    @Column(name = "user_id")
    private Long userId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            insertable = false,
            updatable = false,
            foreignKey = @ForeignKey(name = "fk_friend_qr_code_user")
    )
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @Column(nullable = false, unique = true, length = 36)
    private String code;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    private FriendQrCode(User user) {
        this.userId = user.getId();
        this.user = user;
        reissue();
    }

    public static FriendQrCode create(User user) {
        return new FriendQrCode(user);
    }

    public boolean isExpired() {
        return expiresAt.isBefore(LocalDateTime.now());
    }

    public void reissue() {
        this.code = UUID.randomUUID().toString();
        this.expiresAt = LocalDateTime.now().plusMinutes(TTL_MINUTES);
    }
}
