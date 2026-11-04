package com.sehwan.YakSok.user.event;

public record FriendshipChangedEvent(
        Long targetUserId,
        Long actorUserId,
        String actorNickname,
        Kind kind,
        Long requestId)
{
    public enum Kind { REQUESTED, ACCEPTED }
}
