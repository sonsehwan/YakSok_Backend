package com.sehwan.YakSok.yaksok.event;

public record NotificationStatusChangedEvent(
        Long notificationId,
        Long yaksokId,
        Long ownerId,
        String ownerNickname,
        String yaksokTitle,
        String timeCategory,
        String date,
        boolean isTaken
) {
}
