package com.sehwan.YakSok.user.dto.response;

import com.sehwan.YakSok.user.entity.RelationStatus;
import com.sehwan.YakSok.user.entity.User;
import lombok.Getter;

@Getter
public class UserProfileDto {
    private final Long userId;
    private final String nickname;
    private final String email;
    private final RelationStatus relation;
    private final Long requestId; // relation이 RECEIVED일 때만 값이 있음 (요청 수락/거절에 필요)

    private UserProfileDto(Long userId, String nickname, String email, RelationStatus relation, Long requestId) {
        this.userId = userId;
        this.nickname = nickname;
        this.email = email;
        this.relation = relation;
        this.requestId = requestId;
    }

    public static UserProfileDto of(User user, RelationStatus relation, Long requestId) {
        return new UserProfileDto(user.getId(), user.getNickname(), user.getEmail(), relation, requestId);
    }
}
