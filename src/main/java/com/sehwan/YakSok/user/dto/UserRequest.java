package com.sehwan.YakSok.user.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sehwan.YakSok.user.entity.User;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRequest {

    // 1단계 입력 정보
    private String email;
    private String password;

    // 2단계 입력 정보
    private String nickname;
    private String gender;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyyMMdd")
    private LocalDate birthdate;

    // 추가 정보 (필요 시 안드로이드에서 기본값으로 전송)
    private String fcmToken;

    /**
     * DTO를 User 엔티티로 변환하는 메서드
     * 서비스 계층에서 DB에 저장할 때 사용됩니다.
     */
    public User toEntity() {
        return User.builder()
                .email(email)
                .password(password)
                .nickname(nickname)
                .gender(gender)
                .birthdate(birthdate)
                .fcmToken(fcmToken)
                .penaltyEnable(false) // 초기 가입 시 기본값
                .isLocked(false)      // 초기 가입 시 기본값
                .build();
    }
}