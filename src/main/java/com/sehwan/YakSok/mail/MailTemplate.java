package com.sehwan.YakSok.mail;

public final class MailTemplate {

    private MailTemplate() {}

    private static final String SHELL = """
            <div style="font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,'Apple SD Gothic Neo','Malgun Gothic',sans-serif;max-width:480px;margin:0 auto;padding:32px 24px;color:#222;line-height:1.6;">
              <div style="font-size:20px;font-weight:700;color:#1a1a1a;">약속</div>
              <div style="margin:4px 0 24px;color:#888;font-size:13px;">%s</div>
              %s
            </div>
            """;

    private static final String CODE_BOX = """
            <div style="background:#f4f4f5;border-radius:12px;padding:22px;text-align:center;margin-bottom:20px;">
              <span style="font-size:30px;font-weight:700;letter-spacing:8px;color:#111;">%s</span>
            </div>
            """;

    public static String verificationCode(String actionLabel, String code) {
        String body = CODE_BOX.formatted(code)
                + "<p style=\"margin:0;font-size:14px;\">5분 이내에 입력해주세요.</p>"
                + "<p style=\"margin:8px 0 0;font-size:13px;color:#888;\">본인이 요청하지 않았다면 이 메일을 무시하셔도 됩니다.</p>";
        return SHELL.formatted(actionLabel + " 인증코드", body);
    }

    /** 임시 비밀번호 발급 메일 */
    public static String tempPassword(String password) {
        String body = """
                <div style="background:#f4f4f5;border-radius:12px;padding:22px;text-align:center;margin-bottom:20px;">
                  <span style="font-size:22px;font-weight:700;letter-spacing:2px;color:#111;">%s</span>
                </div>
                <p style="margin:0;font-size:14px;">이 임시 비밀번호로 로그인한 뒤 <b>반드시 새 비밀번호로 변경</b>해주세요.</p>
                """.formatted(password);
        return SHELL.formatted("임시 비밀번호 발급", body);
    }
}
