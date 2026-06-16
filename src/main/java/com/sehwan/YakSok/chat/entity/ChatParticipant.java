package com.sehwan.YakSok.chat.entity;

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
public class ChatParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "chatting_room_id")
    private ChattingRoom chattingRoom;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public ChatParticipant(User user, ChattingRoom chattingRoom) {
        this.user = user;
        this.chattingRoom = chattingRoom;
        this.createdAt = LocalDateTime.now();
    }
}
