package com.sehwan.YakSok.user.event;

public record FriendshipChangedEvent(
        Long targetUserId,
        Long actorUserId,
        String actorNickname,
        Kind kind)
{
    public enum Kind { REQUESTED, ACCEPTED }
}
