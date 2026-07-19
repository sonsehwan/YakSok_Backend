package com.sehwan.YakSok.user.entity;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@EqualsAndHashCode
public class FriendRequestId {
    private String userId;
    private String friendId;
}
