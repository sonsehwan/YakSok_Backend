package com.sehwan.YakSok.chat.event;

public record ChatMessageSavedEvent(
        Long roomId,
        Long senderParticipantId,
        String senderName,
        String preview
) { }
