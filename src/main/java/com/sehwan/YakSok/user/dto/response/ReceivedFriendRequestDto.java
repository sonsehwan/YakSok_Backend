package com.sehwan.YakSok.user.dto.response;

import com.sehwan.YakSok.user.entity.FriendRequest;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ReceivedFriendRequestDto {
    private final Long requestId;
    private final Long requesterId;
    private final String nickname;
    private final String email;
    private final LocalDateTime requestedAt;

    private ReceivedFriendRequestDto(
            Long requestId,
            Long requesterId,
            String nickname,
            String email,
            LocalDateTime requestedAt
    ) {
        this.requestId = requestId;
        this.requesterId = requesterId;
        this.nickname = nickname;
        this.email = email;
        this.requestedAt = requestedAt;
    }

    public static ReceivedFriendRequestDto from(FriendRequest friendRequest){
        return new ReceivedFriendRequestDto(
                friendRequest.getId(),
                friendRequest.getRequester().getId(),
                friendRequest.getRequester().getNickname(),
                friendRequest.getRequester().getEmail(),
                friendRequest.getRequestedAt()
        );
    }
}
