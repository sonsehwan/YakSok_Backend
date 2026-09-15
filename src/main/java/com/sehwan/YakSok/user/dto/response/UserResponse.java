package com.sehwan.YakSok.user.dto.response;

import com.sehwan.YakSok.drugstore.entity.DrugStore;
import com.sehwan.YakSok.user.entity.User;
import com.sehwan.YakSok.user.entity.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String email;

    private String nickname;

    private UserRole role;

    private DrugStore myDrugStore; // 일반 유저는 null

    private boolean notificationEnabled;

    public UserResponse(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.nickname = user.getNickname();
        this.role = user.getRole();
        this.myDrugStore = user.getMyDrugStore();
        this.notificationEnabled = user.isNotificationEnabled();
    }
}