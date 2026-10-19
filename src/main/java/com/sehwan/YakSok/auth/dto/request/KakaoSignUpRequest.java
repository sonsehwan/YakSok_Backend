package com.sehwan.YakSok.auth.dto.request;

import com.sehwan.YakSok.drugstore.dto.SearchDrugStoreDto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class KakaoSignUpRequest {

    // 신규 가입은 요청이 두 번으로 나뉘므로 카카오 토큰을 다시 받아 서버가 다시 검증한다
    @NotBlank(message = "카카오 토큰이 없습니다.")
    private String accessToken;

    // 일반 가입과 달리 role이 빠진 요청을 기본값으로 받아주지 않는다
    @NotBlank(message = "회원 유형을 선택해주세요.")
    @Pattern(regexp = "^(NORMAL|DRUGSTORE)$", message = "회원 유형이 올바르지 않습니다.")
    private String role;

    @NotBlank(message = "닉네임을 입력해주세요.")
    @Pattern(regexp = "^[a-zA-Z0-9가-힣]{2,10}$",
            message = "닉네임은 2~10자의 영문·한글·숫자만 사용 가능합니다.")
    private String nickname;

    private SearchDrugStoreDto drugStore;
}
