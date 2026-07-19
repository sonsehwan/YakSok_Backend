package com.sehwan.YakSok.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Getter
@Embeddable
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FriendRelationId implements Serializable {

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "friend_id")
    private Long friendId;
}