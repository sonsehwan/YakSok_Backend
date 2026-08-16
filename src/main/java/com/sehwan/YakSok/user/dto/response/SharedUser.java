package com.sehwan.YakSok.user.dto.response;


import com.sehwan.YakSok.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SharedUser {
    Long userId;
    String nickName;

    public static SharedUser from(User user){
        return new SharedUser(
                user.getId(),
                user.getNickname()
        );
    }
}


