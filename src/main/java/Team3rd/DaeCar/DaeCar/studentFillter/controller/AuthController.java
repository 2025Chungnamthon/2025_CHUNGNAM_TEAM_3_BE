package Team3rd.DaeCar.DaeCar.studentFillter.controller;

import Team3rd.DaeCar.DaeCar.studentFillter.dto.CodeVerificationRequest;
import Team3rd.DaeCar.DaeCar.studentFillter.dto.EmailRequest;
import Team3rd.DaeCar.DaeCar.studentFillter.dto.SignUpRequest;
import Team3rd.DaeCar.DaeCar.studentFillter.service.AuthCodeStorage;
import Team3rd.DaeCar.DaeCar.studentFillter.service.AuthService;
import Team3rd.DaeCar.DaeCar.studentFillter.utill.SchoolDomainMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/send")
    public ResponseEntity<?> sendAuthCode(@RequestBody @Valid EmailRequest request) {
        String email = request.getEmail();

        // 학교 지원 여부 체크
        String school = SchoolDomainMapper.getSchoolByEmail(email);
        if (school == null) {
            return ResponseEntity.badRequest().body("지원하지 않는 학교 이메일입니다.");
        }

        // 인증 코드 전송
        authService.sendAuthCode(email);

        // 필요하다면 인증 코드 전송 성공 메시지에 학교명 포함 가능
        return ResponseEntity.ok("인증코드 전송 완료 (학교: " + school + ")");
    }



    @PostMapping("/verify")
    public ResponseEntity<?> verifyCode(@RequestBody @Valid CodeVerificationRequest request) {

        String email = request.getEmail();
        String code = request.getCode();

        if (!authService.isCodePresent(email)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("인증 코드가 존재하지 않거나 만료되었습니다. 먼저 인증 코드를 요청해주세요.");
        }

        boolean verified = authService.verifyCode(email, code);
        if (verified) {
            return ResponseEntity.ok("인증 성공");
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("인증 코드가 유효하지 않습니다.");
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signUp(@RequestBody @Valid SignUpRequest request) {

        return ResponseEntity.ok("정보 입력이 완료되었습니다.");
    }
}
