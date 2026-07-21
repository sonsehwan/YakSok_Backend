package com.sehwan.YakSok.user.dto.response;

import com.sehwan.YakSok.user.entity.FriendRelation;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class FriendListDto {

    private final List<FriendResponseDto> friends;
    private final int totalCount;

    private FriendListDto(List<FriendResponseDto> friends) {
        this.friends = friends;
        this.totalCount = friends.size();
    }

    public static FriendListDto from(List<FriendRelation> friendRelations) {
        List<FriendResponseDto> dtoList = friendRelations.stream()
                .map(FriendResponseDto::from)
                .collect(Collectors.toList());

        return new FriendListDto(dtoList);
    }

}