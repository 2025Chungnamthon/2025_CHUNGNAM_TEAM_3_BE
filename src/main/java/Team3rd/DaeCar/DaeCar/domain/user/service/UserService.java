package Team3rd.DaeCar.DaeCar.domain.user.service;

import Team3rd.DaeCar.DaeCar.domain.user.dto.*;
import Team3rd.DaeCar.DaeCar.domain.user.entity.User;
import Team3rd.DaeCar.DaeCar.domain.user.repository.UserRepository;
import Team3rd.DaeCar.DaeCar.domain.student.service.AuthService;
import Team3rd.DaeCar.DaeCar.domain.student.util.SchoolDomainMapper;
import Team3rd.DaeCar.DaeCar.global.util.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthService authService;
    private final SchoolDomainMapper schoolDomainMapper;
    
    public UserService(UserRepository userRepository, 
                      PasswordEncoder passwordEncoder,
                      JwtUtil jwtUtil,
                      AuthService authService,
                      SchoolDomainMapper schoolDomainMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authService = authService;
        this.schoolDomainMapper = schoolDomainMapper;
    }
    
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByEmailAndIsActiveTrue(request.getEmail())) {
            throw new RuntimeException("이미 존재하는 이메일입니다.");
        }
        
        if (userRepository.existsByNicknameAndIsActiveTrue(request.getNickname())) {
            throw new RuntimeException("이미 존재하는 닉네임입니다.");
        }
        
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhoneNumber(request.getPhoneNumber());
        user.setGender(request.getGender());
        user.setNickname(request.getNickname());
        user.setUniversity(request.getUniversity());
        user.setDriverLicenseVerified(false);
        user.setIsActive(true);
        
        User savedUser = userRepository.save(user);
        return new UserResponse(savedUser);
    }
    
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmailAndIsActiveTrue(request.getEmail())
                .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다."));
        
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("비밀번호가 올바르지 않습니다.");
        }
        
        String token = jwtUtil.generateToken(user.getEmail());
        UserResponse userResponse = new UserResponse(user);
        
        return new LoginResponse(token, userResponse);
    }
    
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long userId) {
        User user = userRepository.findByIdAndIsActiveTrue(userId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다."));
        return new UserResponse(user);
    }
    
    @Transactional(readOnly = true)
    public UserResponse getUserByEmail(String email) {
        User user = userRepository.findByEmailAndIsActiveTrue(email)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다."));
        return new UserResponse(user);
    }
    
    public void updateDriverLicenseVerification(Long userId, boolean verified) {
        User user = userRepository.findByIdAndIsActiveTrue(userId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다."));
        
        user.setDriverLicenseVerified(verified);
        userRepository.save(user);
    }
    
    public StudentVerificationResponse requestStudentVerification(Long userId, StudentVerificationRequest request) {
        User user = userRepository.findByIdAndIsActiveTrue(userId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다."));
        
        user.setStudentVerificationStatus(User.StudentVerificationStatus.PENDING);
        userRepository.save(user);
        
        return new StudentVerificationResponse(
            userId, 
            User.StudentVerificationStatus.PENDING, 
            "대학생 인증 요청이 접수되었습니다."
        );
    }
    
    public void updateStudentVerificationStatus(Long userId, User.StudentVerificationStatus status) {
        User user = userRepository.findByIdAndIsActiveTrue(userId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다."));
        
        user.setStudentVerificationStatus(status);
        userRepository.save(user);
    }
    
    public void sendStudentEmailVerification(Long userId, String studentEmail) {
        User user = userRepository.findByIdAndIsActiveTrue(userId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다."));
        
        String universityName = user.getUniversity();
        if (!schoolDomainMapper.isValidEmailForUniversity(studentEmail, universityName)) {
            throw new RuntimeException("해당 대학교 이메일 도메인이 아닙니다.");
        }
        
        user.setStudentEmail(studentEmail);
        user.setStudentEmailVerified(false);
        userRepository.save(user);
        
        authService.sendAuthCode(studentEmail);
    }
    
    public boolean verifyStudentEmail(Long userId, String verificationCode) {
        User user = userRepository.findByIdAndIsActiveTrue(userId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다."));
        
        if (user.getStudentEmail() == null) {
            throw new RuntimeException("학생 이메일이 등록되지 않았습니다.");
        }
        
        boolean verified = authService.verifyCode(user.getStudentEmail(), verificationCode);
        
        if (verified) {
            user.setStudentEmailVerified(true);
            user.setStudentVerificationStatus(User.StudentVerificationStatus.VERIFIED);
            userRepository.save(user);
        }
        
        return verified;
    }
    
    @Transactional(readOnly = true)
    public boolean isStudentVerified(Long userId) {
        User user = userRepository.findByIdAndIsActiveTrue(userId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다."));
        
        return user.getStudentVerificationStatus() == User.StudentVerificationStatus.VERIFIED 
               && user.getStudentEmailVerified();
    }
}