package com.sehwan.YakSok.user.dto;

import com.sehwan.YakSok.drugstore.entity.DrugStore;
import com.sehwan.YakSok.user.entity.User;
import com.sehwan.YakSok.user.entity.UserRole;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDate;

@Getter
@ToString
public class UserResponse {
    private String email;

    private String password;

    private String nickname;

    private String gender;

    private LocalDate birthdate;


    private Boolean penaltyEnable = false;

    private Boolean isLocked = false;

    private UserRole role;

    private DrugStore myDrugStore; // 일반 유저는 null

    public UserResponse(User user) {
        this.email = user.getEmail();
        this.password = user.getPassword();
        this.nickname = user.getNickname();
        this.gender = user.getGender();
        this.birthdate = user.getBirthdate();
        this.role = user.getRole();
        this.myDrugStore = user.getMyDrugStore();
    }
}
