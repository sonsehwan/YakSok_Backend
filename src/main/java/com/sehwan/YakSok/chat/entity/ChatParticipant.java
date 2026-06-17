package com.sehwan.YakSok.chat.entity;

import com.sehwan.YakSok.drugstore.entity.DrugStore;
import com.sehwan.YakSok.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

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

    // 2. 일반 유저 매핑 (User의 PK가 email이므로 참조 컬럼명을 user_email로 설정)
    // 약국이 참여자일 경우 이 필드는 DB에 null로 저장됨
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_email", nullable = true)
    private User user;

    // 4. 소속 채팅방
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chatting_room_id", nullable = false)
    private ChattingRoom chattingRoom;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public ChatParticipant(User user, ChattingRoom chattingRoom) {
        this.user = user;
        this.chattingRoom = chattingRoom;
    }
}