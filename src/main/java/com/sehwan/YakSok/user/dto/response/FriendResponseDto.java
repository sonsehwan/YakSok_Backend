package com.sehwan.YakSok.user.dto.response;

import com.sehwan.YakSok.user.entity.FriendRelation;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;


/**
 * 단일 친구 정보
 */
@Getter
@Setter
public class FriendResponseDto {

    private final Long friendId;
    private final String nickname;
    private final String email;
    private final LocalDateTime friendedAt;

    private FriendResponseDto(
            Long friendId,
            String nickname,
            String email,
            LocalDateTime friendedAt
    ) {
        this.friendId = friendId;
        this.nickname = nickname;
        this.email = email;
        this.friendedAt = friendedAt;
    }

    // 친구 관계 entity에서 친구 entity를 가져와 필요한 정보만 뽑아서 담는다.
    public static FriendResponseDto from(FriendRelation friendRelation) {
        return new FriendResponseDto(
                friendRelation.getFriend().getId(),
                friendRelation.getFriend().getNickname(),
                friendRelation.getFriend().getEmail(),
                friendRelation.getCreatedAt()
        );
    }

}