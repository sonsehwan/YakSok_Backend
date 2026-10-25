package com.sehwan.YakSok.chat.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long roomId;

    // 이 방에서 발신자를 대표하는 ChatParticipant의 id.
    // 하드 FK를 걸지 않는다 — 참여자가 방을 나가도(행 삭제) 지난 메시지는 남아야 하기 때문.
    @Column(nullable = false)
    private Long senderParticipantId;

    // 보낼 당시의 발신자 이름 스냅샷. 닉네임/약국명이 바뀌거나 탈퇴해도 과거 말풍선은 그대로.
    @Column(nullable = false)
    private String senderName;

    @Column(nullable = false, length = 1000)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private MessageType type = MessageType.TEXT;

    private Long yaksokId;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

}

