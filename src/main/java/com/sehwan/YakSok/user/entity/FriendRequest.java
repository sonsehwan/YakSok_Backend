package com.sehwan.YakSok.user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(
        name = "friend_request",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_friend_request_pair",
                        columnNames = {
                                "user_low_id",
                                "user_high_id"
                        }
                )
        },
        indexes = {
                @Index(
                        name = "idx_friend_request_requester",
                        columnList = "requester_id"
                ),
                @Index(
                        name = "idx_friend_request_receiver",
                        columnList = "receiver_id"
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FriendRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "requester_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_friend_request_requester"
            )
    )
    private User requester;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "receiver_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_friend_request_receiver"
            )
    )
    private User receiver;

    @Column(name = "user_low_id", nullable = false)
    private Long userLowId;

    @Column(name = "user_high_id", nullable = false)
    private Long userHighId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private FriendRequestStatus status;

    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt;

    @Column(name = "responded_at")
    private LocalDateTime respondedAt;

    private FriendRequest(
            User requester,
            User receiver
    ) {
        validateUsers(requester, receiver);

        this.requester = requester;
        this.receiver = receiver;

        setUserPair(requester.getId(), receiver.getId());

        this.status = FriendRequestStatus.PENDING;
        this.requestedAt = LocalDateTime.now();
        this.respondedAt = null;
    }

    public static FriendRequest create(
            User requester,
            User receiver
    ) {
        return new FriendRequest(requester, receiver);
    }

    /**
     * 친구 요청 수락
     */
    public void accept() {
        if (this.status != FriendRequestStatus.PENDING) {
            throw new IllegalStateException(
                    "대기 중인 친구 요청만 수락할 수 있습니다."
            );
        }

        this.status = FriendRequestStatus.ACCEPTED;
        this.respondedAt = LocalDateTime.now();
    }

    /**
     * 친구 요청 거절
     */
    public void reject() {
        if (this.status != FriendRequestStatus.PENDING) {
            throw new IllegalStateException(
                    "대기 중인 친구 요청만 거절할 수 있습니다."
            );
        }

        this.status = FriendRequestStatus.REJECTED;
        this.respondedAt = LocalDateTime.now();
    }

    /**
     * 거절된 친구 요청을 다시 PENDING으로 변경
     */
    public void restart(
            User newRequester,
            User newReceiver
    ) {
        if (this.status != FriendRequestStatus.REJECTED) {
            throw new IllegalStateException(
                    "거절된 친구 요청만 다시 요청할 수 있습니다."
            );
        }

        validateUsers(newRequester, newReceiver);

        this.requester = newRequester;
        this.receiver = newReceiver;

        setUserPair(
                newRequester.getId(),
                newReceiver.getId()
        );

        this.status = FriendRequestStatus.PENDING;
        this.requestedAt = LocalDateTime.now();
        this.respondedAt = null;
    }

    /**
     * 현재 사용자가 요청을 보낸 사람인지 확인
     */
    public boolean isRequestedBy(Long userId) {
        return requester.getId().equals(userId);
    }

    /**
     * 현재 사용자가 요청을 받은 사람인지 확인
     */
    public boolean isReceivedBy(Long userId) {
        return receiver.getId().equals(userId);
    }

    /**
     * 대기 중인 요청인지 확인
     */
    public boolean isPending() {
        return this.status == FriendRequestStatus.PENDING;
    }

    private void setUserPair(
            Long firstUserId,
            Long secondUserId
    ) {
        this.userLowId = Math.min(firstUserId, secondUserId);
        this.userHighId = Math.max(firstUserId, secondUserId);
    }

    private void validateUsers(
            User requester,
            User receiver
    ) {
        if (requester == null || receiver == null) {
            throw new IllegalArgumentException(
                    "요청자와 응답자는 필수입니다."
            );
        }

        if (requester.getId() == null || receiver.getId() == null) {
            throw new IllegalArgumentException(
                    "저장된 사용자만 친구 요청을 할 수 있습니다."
            );
        }

        if (requester.getId().equals(receiver.getId())) {
            throw new IllegalArgumentException(
                    "자기 자신에게 친구 요청을 보낼 수 없습니다."
            );
        }
    }
}