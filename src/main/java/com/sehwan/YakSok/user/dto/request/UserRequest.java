package com.sehwan.YakSok.user.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sehwan.YakSok.user.entity.User;
import com.sehwan.YakSok.user.entity.UserRole;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRequest {

    private String email;
    private String password;

    private String nickname;
    private String gender;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyyMMdd")
    private LocalDate birthdate;

    private String fcmToken;

    private String role;

    /**
     * DTO를 User 엔티티로 변환하는 메서드
     * 서비스 계층에서 DB에 저장할 때 사용됩니다.
     */
    public User toEntity() {
        UserRole userRole = this.role != null && this.role.equals("DRUGSTORE")
                ? UserRole.DRUGSTORE
                : UserRole.NORMAL;

        return User.builder()
                .email(email)
                .password(password)
                .nickname(nickname)
                .gender(gender)
                .birthdate(birthdate)
                .fcmToken(fcmToken)
                .role(userRole)
                .penaltyEnable(false) // 초기 가입 시 기본값
                .isLocked(false)      // 초기 가입 시 기본값
                .myDrugStore(null)
                .build();
    }
}