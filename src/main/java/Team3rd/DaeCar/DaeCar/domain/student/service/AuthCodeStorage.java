package Team3rd.DaeCar.DaeCar.domain.student.service;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AuthCodeStorage {

    private final Map<String, AuthCode> codeMap = new ConcurrentHashMap<>();

    private static final Map<String, String> ALLOWED_SCHOOL_DOMAINS = Map.of(
            "smail.kongju.ac.kr", "공주대학교",
            "bu.ac.kr", "백석대학교",
            "sangmyung.kr", "상명대학교",
            "vision.hoseo.edu", "호서대학교",
            "dankook.ac.kr", "단국대학교"
    );

    public boolean isAllowedDomain(String email, String school) {
        String allowedDomain = ALLOWED_SCHOOL_DOMAINS.get(school);
        if (allowedDomain == null) return false;

        return email.endsWith("@" + allowedDomain);
    }
    public void saveCode(String email, String code, LocalDateTime expiryTime) {
        codeMap.put(email, new AuthCode(code, expiryTime));
    }

    public boolean verifyCode(String email, String code) {
        AuthCode authCode = codeMap.get(email);
        if (authCode == null) {
            return false;
        }
        if (authCode.isExpired()) {
            codeMap.remove(email);
            return false;
        }
        return authCode.getCode().equals(code);
    }

    public void removeCode(String email) {
        codeMap.remove(email);
    }

    public boolean isCodePresent(String email) {
        AuthCode authCode = codeMap.get(email);
        if (authCode == null) return false;
        if (authCode.isExpired()) {
            codeMap.remove(email);
            return false;
        }
        return true;
    }

    // 내부 클래스 또는 별도 파일로 분리 가능
    public static class AuthCode {
        private final String code;
        private final LocalDateTime expiryTime;

        public AuthCode(String code, LocalDateTime expiryTime) {
            this.code = code;
            this.expiryTime = expiryTime;
        }

        public String getCode() {
            return code;
        }

        public boolean isExpired() {
            return LocalDateTime.now().isAfter(expiryTime);
        }
    }
}