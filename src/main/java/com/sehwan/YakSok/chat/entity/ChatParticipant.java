package com.sehwan.YakSok.chat.entity;

import com.sehwan.YakSok.drugstore.entity.DrugStore;
import com.sehwan.YakSok.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "chat_participant")
public class ChatParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 참여자가 개인 유저일 때만 채워진다. 약국 참여자면 null.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    // 참여자가 약국일 때만 채워진다. 개인 참여자면 null.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "drug_store_id", nullable = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private DrugStore drugStore;

    // USER면 user != null && drugStore == null, DRUGSTORE면 반대.
    // 불변식은 아래 편의 생성자로만 세팅해 지킨다 (JPA는 강제 못 함).
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ParticipantType participantType;

    // 소속 채팅방
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chatting_room_id", nullable = false)
    private ChattingRoom chattingRoom;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private boolean hidden = false;

    private LocalDateTime joinedAt;

    // 개인 유저 참여자
    public ChatParticipant(User user, ChattingRoom chattingRoom) {
        this.user = user;
        this.chattingRoom = chattingRoom;
        this.participantType = ParticipantType.USER;
    }

    // 약국 참여자
    public ChatParticipant(DrugStore drugStore, ChattingRoom chattingRoom) {
        this.drugStore = drugStore;
        this.chattingRoom = chattingRoom;
        this.participantType = ParticipantType.DRUGSTORE;
    }

    public boolean isUser() {
        return participantType == ParticipantType.USER;
    }

    public boolean isDrugStore() {
        return participantType == ParticipantType.DRUGSTORE;
    }

    // 이 방에서 이 참여자를 부를 이름 (개인=닉네임, 약국=약국명)
    public String displayName() {
        return isDrugStore() ? drugStore.getDutyName() : user.getNickname();
    }
}
