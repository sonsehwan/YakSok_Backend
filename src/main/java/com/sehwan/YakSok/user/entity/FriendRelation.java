package com.sehwan.YakSok.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.*;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(
        name = "friend_relation",
        indexes = {
                @Index(
                        name = "idx_friend_relation_friend",
                        columnList = "friend_id"
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FriendRelation {

    @EmbeddedId
    private FriendRelationId id;

    @MapsId("userId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_friend_relation_user"
            )
    )
    private User user;

    @MapsId("friendId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "friend_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_friend_relation_friend"
            )
    )
    private User friend;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    private FriendRelation(
            User user,
            User friend
    ) {
        validateUsers(user, friend);

        this.id = new FriendRelationId(
                user.getId(),
                friend.getId()
        );

        this.user = user;
        this.friend = friend;
        this.createdAt = LocalDateTime.now();
    }

    public static FriendRelation create(
            User user,
            User friend
    ) {
        return new FriendRelation(user, friend);
    }

    private void validateUsers(
            User user,
            User friend
    ) {
        if (user == null || friend == null) {
            throw new IllegalArgumentException(
                    "사용자와 친구 정보는 필수입니다."
            );
        }

        if (user.getId() == null || friend.getId() == null) {
            throw new IllegalArgumentException(
                    "저장된 사용자만 친구 관계를 만들 수 있습니다."
            );
        }

        if (user.getId().equals(friend.getId())) {
            throw new IllegalArgumentException(
                    "자기 자신을 친구로 등록할 수 없습니다."
            );
        }
    }
}
