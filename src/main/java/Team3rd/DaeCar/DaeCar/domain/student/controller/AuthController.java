package Team3rd.DaeCar.DaeCar.domain.student.controller;

import Team3rd.DaeCar.DaeCar.domain.student.dto.*;
import Team3rd.DaeCar.DaeCar.domain.student.service.AuthCodeStorage;
import Team3rd.DaeCar.DaeCar.domain.student.service.AuthService;
import Team3rd.DaeCar.DaeCar.domain.student.util.SchoolDomainMapper;
import Team3rd.DaeCar.DaeCar.domain.user.entity.User;
import Team3rd.DaeCar.DaeCar.domain.user.repository.UserRepository;
import Team3rd.DaeCar.DaeCar.domain.user.service.UserService;
import Team3rd.DaeCar.DaeCar.global.util.JwtUtil;
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
    private final UserRepository userRepository;
    private final UserService userService;
    private final JwtUtil jwtUtil;

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
            // 사용자 존재 여부 확인
            boolean userExists = userRepository.existsByEmailAndIsActiveTrue(email);
            
            // 기존 사용자인 경우 학생 인증 상태 업데이트
            if (userExists) {
                User user = userRepository.findByEmailAndIsActiveTrue(email).orElse(null);
                if (user != null) {
                    user.setStudentEmail(email);
                    user.setStudentEmailVerified(true);
                    user.setStudentVerificationStatus(User.StudentVerificationStatus.VERIFIED);
                    userRepository.save(user);
                }
            }
            
            return ResponseEntity.ok(new UserStatusResponse(userExists, 
                userExists ? "기존 사용자입니다. 학생 인증이 완료되었습니다. 비밀번호를 입력해주세요." : "신규 사용자입니다. 회원가입을 진행해주세요.", 
                email));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("인증 코드가 유효하지 않습니다.");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid LoginRequest request) {
        try {
            // 사용자 존재 확인
            User user = userRepository.findByEmailAndIsActiveTrue(request.getEmail())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
            
            // 비밀번호 확인은 UserService의 로그인 로직 사용
            Team3rd.DaeCar.DaeCar.domain.user.dto.LoginRequest userLoginRequest = 
                new Team3rd.DaeCar.DaeCar.domain.user.dto.LoginRequest();
            userLoginRequest.setEmail(request.getEmail());
            userLoginRequest.setPassword(request.getPassword());
            
            Team3rd.DaeCar.DaeCar.domain.user.dto.LoginResponse loginResponse = 
                userService.login(userLoginRequest);
            
            return ResponseEntity.ok(loginResponse);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("이메일 또는 비밀번호가 올바르지 않습니다.");
        }
    }
    
    @PostMapping("/signup")
    public ResponseEntity<?> signUp(@RequestBody @Valid SignUpRequest request) {
        try {
            // 학교 도메인에서 대학교 이름 추출
            String university = SchoolDomainMapper.getSchoolByEmail(request.getEmail());
            if (university == null) {
                return ResponseEntity.badRequest().body("지원하지 않는 학교 이메일입니다.");
            }
            
            // User 회원가입 요청 변환
            Team3rd.DaeCar.DaeCar.domain.user.dto.RegisterRequest registerRequest = 
                new Team3rd.DaeCar.DaeCar.domain.user.dto.RegisterRequest();
            registerRequest.setEmail(request.getEmail());
            registerRequest.setPassword(request.getPassword());
            registerRequest.setNickname(request.getNickname());
            registerRequest.setPhoneNumber(request.getPhoneNumber());
            registerRequest.setGender(User.Gender.valueOf(request.getGender().toUpperCase()));
            registerRequest.setUniversity(university);
            
            // 회원가입 처리
            Team3rd.DaeCar.DaeCar.domain.user.dto.UserResponse userResponse = 
                userService.register(registerRequest);
                
            // 회원가입 후 학생 인증 상태 자동 설정 (학교 이메일로 가입했으므로)
            User newUser = userRepository.findByEmailAndIsActiveTrue(request.getEmail()).orElse(null);
            if (newUser != null) {
                newUser.setStudentEmail(request.getEmail());
                newUser.setStudentEmailVerified(true);
                newUser.setStudentVerificationStatus(User.StudentVerificationStatus.VERIFIED);
                userRepository.save(newUser);
            }
                
            // JWT 토큰 생성
            String token = jwtUtil.generateToken(request.getEmail());
            
            // 로그인 응답 생성
            Team3rd.DaeCar.DaeCar.domain.user.dto.LoginResponse loginResponse = 
                new Team3rd.DaeCar.DaeCar.domain.user.dto.LoginResponse();
            loginResponse.setToken(token);
            loginResponse.setUser(userResponse);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(loginResponse);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("회원가입 중 오류가 발생했습니다.");
        }
    }
}