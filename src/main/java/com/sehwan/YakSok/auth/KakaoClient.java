package com.sehwan.YakSok.auth;

import tools.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class KakaoClient {

    private static final String INFO_URL = "https://kapi.kakao.com/v1/user/access_token_info";
    private static final String ME_URL = "https://kapi.kakao.com/v2/user/me";

    private final RestClient restClient;
    private final String appId;

    public KakaoClient(@Qualifier("restClient") RestClient restClient, @Value("${kakao.app-id}") String appId) {
        this.restClient = restClient;
        this.appId = appId;
    }

    public record KakaoProfile(String id, String email, boolean emailVerified, String nickname) {
    }

    public KakaoProfile fetchProfile(String accessToken) {
        try {
            // 다른 카카오 앱에서 발급된 토큰이면 /v2/user/me는 그래도 응답하므로, 먼저 우리 앱 토큰인지 확인한다
            JsonNode info = get(INFO_URL, accessToken);
            if (appId.isBlank() || !appId.equals(info.path("app_id").asText())) {
                throw new IllegalArgumentException("유효하지 않은 카카오 토큰입니다.");
            }

            JsonNode me = get(ME_URL, accessToken);
            JsonNode account = me.path("kakao_account");
            JsonNode profile = account.path("profile");

            // 기본 닉네임("닉네임을 등록해주세요")이면 미리 채울 값이 없는 것으로 본다
            String nickname = profile.path("is_default_nickname").asBoolean(false)
                    ? null : profile.path("nickname").asText(null);

            return new KakaoProfile(
                    me.path("id").asText(),
                    account.path("email").asText(null),
                    account.path("is_email_valid").asBoolean(false)
                            && account.path("is_email_verified").asBoolean(false),
                    nickname);
        } catch (RestClientException e) {
            // 만료·위조 토큰(4xx)과 카카오 장애(5xx)를 앱이 구분할 필요는 없다. 401은 앱의 토큰 재발급 로직을 건드리므로 쓰지 않는다
            throw new IllegalArgumentException("카카오 인증에 실패했습니다. 다시 시도해주세요.", e);
        }
    }

    private JsonNode get(String url, String accessToken) {
        return restClient.get().uri(url)
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .body(JsonNode.class);
    }
}
