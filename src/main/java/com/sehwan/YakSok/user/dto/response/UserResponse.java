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

    private String socialType; // 일반 가입 회원은 null, 카카오 연동 회원은 "KAKAO"

    private boolean hasPassword; // 카카오 연동 회원과 카카오 전용 회원을 앱이 구분(비밀번호 변경 메뉴)

    private DrugStore myDrugStore; // 일반 유저는 null

    public UserResponse(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.nickname = user.getNickname();
        this.role = user.getRole();
        this.socialType = user.getSocialType();
        this.hasPassword = user.getPassword() != null;
        this.myDrugStore = user.getMyDrugStore();
    }
}