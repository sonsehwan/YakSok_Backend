package com.sehwan.YakSok.user.dto.response;

import com.sehwan.YakSok.drugstore.entity.DrugStore;
import com.sehwan.YakSok.user.entity.User;
import com.sehwan.YakSok.user.entity.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;

@Getter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String email;

    private String password;

    private String nickname;

    private String gender;

    private LocalDate birthdate;

    private String fcmToken;

    @Builder.Default
    private Boolean penaltyEnable = false;

    @Builder.Default
    private Boolean isLocked = false;

    private UserRole role;

    private DrugStore myDrugStore; // 일반 유저는 null

    public UserResponse(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.password = user.getPassword();
        this.nickname = user.getNickname();
        this.gender = user.getGender();
        this.fcmToken = user.getFcmToken();
        this.birthdate = user.getBirthdate();
        this.role = user.getRole();
        this.myDrugStore = user.getMyDrugStore();
    }
}