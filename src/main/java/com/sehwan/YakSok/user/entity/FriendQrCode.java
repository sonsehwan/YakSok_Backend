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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            unique = true,
            foreignKey = @ForeignKey(name = "fk_friend_qr_code_user")
    )
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @Column(nullable = false, unique = true, length = 36)
    private String code;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    private FriendQrCode(User user) {
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
