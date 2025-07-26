package Team3rd.DaeCar.DaeCar.domain.student.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JavaMailSender mailSender;
    private final AuthCodeStorage authCodeStorage;

    private static final int CODE_EXPIRATION_MINUTES = 5;

    public void sendAuthCode(String email) {
        String code = String.valueOf((int)(Math.random() * 900000) + 100000); // 6자리

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("[대학생 인증] 인증코드를 입력해주세요");
        message.setText("인증코드: " + code + "\n인증코드는 " + CODE_EXPIRATION_MINUTES + "분간 유효합니다.");
        mailSender.send(message);

        LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(CODE_EXPIRATION_MINUTES);
        authCodeStorage.saveCode(email, code, expiryTime);
    }

    public boolean verifyCode(String email, String code) {
        boolean success = authCodeStorage.verifyCode(email, code);
        if (success) {
            authCodeStorage.removeCode(email);
        }
        return success;
    }

    public boolean isCodePresent(String email) {
        return authCodeStorage.isCodePresent(email);
    }
}