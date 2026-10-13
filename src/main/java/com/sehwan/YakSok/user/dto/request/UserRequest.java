package com.sehwan.YakSok.user.dto.request;

import com.sehwan.YakSok.drugstore.dto.SearchDrugStoreDto;
import com.sehwan.YakSok.user.entity.User;
import com.sehwan.YakSok.user.entity.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRequest {

    @NotBlank(message = "이메일을 입력해주세요.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    private String email;

    @NotBlank(message = "아이디를 입력해주세요.")
    @Pattern(regexp = "^[a-z0-9]{8,20}$",
            message = "아이디는 영문 소문자·숫자 조합 8~20자여야 합니다.")
    private String loginId;

    @NotBlank(message = "비밀번호를 입력해주세요.")
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+])[A-Za-z\\d!@#$%^&*()_+]{8,16}$",
            message = "비밀번호는 8~16자의 영문·숫자·특수문자를 모두 포함해야 합니다.")
    private String password;

    @NotBlank(message = "닉네임을 입력해주세요.")
    @Pattern(regexp = "^[a-zA-Z0-9가-힣]{2,10}$",
            message = "닉네임은 2~10자의 영문·한글·숫자만 사용 가능합니다.")
    private String nickname;
    private SearchDrugStoreDto drugStore;
    private String role;

    public User toEntity() {
        UserRole userRole = this.role != null && this.role.equals("DRUGSTORE")
                ? UserRole.DRUGSTORE
                : UserRole.NORMAL;

        return User.builder()
                .email(email)
                .loginId(loginId)
                .password(password)
                .nickname(nickname)
                .role(userRole)
                .myDrugStore(null)
                .build();
    }
}