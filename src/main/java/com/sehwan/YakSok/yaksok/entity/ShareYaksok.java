package com.sehwan.YakSok.yaksok.entity;

import com.sehwan.YakSok.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@Table(
        name = "share_yaksok",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_share_yaksok_receiver_yaksok",
                        columnNames = {"receiver_id", "yaksok_id"}
                )
        }
)
public class ShareYaksok {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name= "receiver_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User receiver;

    @ManyToOne
    @JoinColumn(name = "yaksok_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Yaksok yaksok;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime savedAt;

    private ShareYaksok(User receiver, Yaksok yaksok){
        this.receiver = receiver;
        this.yaksok = yaksok;
    }

    public static ShareYaksok create(User receiver, Yaksok yaksok){
        return new ShareYaksok(receiver, yaksok);
    }

}
