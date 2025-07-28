package Team3rd.DaeCar.DaeCar.domain.user.service;

import Team3rd.DaeCar.DaeCar.domain.user.dto.*;
import Team3rd.DaeCar.DaeCar.domain.user.entity.User;
import Team3rd.DaeCar.DaeCar.domain.user.repository.UserRepository;
import Team3rd.DaeCar.DaeCar.domain.student.service.AuthService;
import Team3rd.DaeCar.DaeCar.domain.student.util.SchoolDomainMapper;
import Team3rd.DaeCar.DaeCar.global.util.JwtUtil;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Service
@Transactional
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthService authService;
    private final SchoolDomainMapper schoolDomainMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    
    private static final String TOKEN_BLACKLIST_PREFIX = "blacklist:token:";
    
    public UserService(UserRepository userRepository, 
                      PasswordEncoder passwordEncoder,
                      JwtUtil jwtUtil,
                      AuthService authService,
                      SchoolDomainMapper schoolDomainMapper,
                      RedisTemplate<String, Object> redisTemplate) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authService = authService;
        this.schoolDomainMapper = schoolDomainMapper;
        this.redisTemplate = redisTemplate;
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
        user.setStudentVerificationStatus(User.StudentVerificationStatus.PENDING);
        
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
    
    public void logout(String token) {
        try {
            // JWT 토큰에서 만료 시간 추출
            //long expirationTime = jwtUtil.getExpirationTime(token);
            long expirationTime = jwtUtil.extractExpiration(token).getTime();
            long currentTime = System.currentTimeMillis();
            
            if (expirationTime > currentTime) {
                // 토큰이 아직 유효한 경우, 블랙리스트에 추가
                long ttl = expirationTime - currentTime;
                String blacklistKey = TOKEN_BLACKLIST_PREFIX + token;
                
                redisTemplate.opsForValue().set(blacklistKey, "LOGOUT", ttl, TimeUnit.MILLISECONDS);
            }
        } catch (Exception e) {
            // Redis 오류가 있어도 로그아웃은 성공으로 처리
            System.err.println("Redis blacklist error: " + e.getMessage());
        }
    }
    
    public boolean isTokenBlacklisted(String token) {
        try {
            String blacklistKey = TOKEN_BLACKLIST_PREFIX + token;
            return redisTemplate.hasKey(blacklistKey);
        } catch (Exception e) {
            // Redis 오류 시 안전상 false 반환 (토큰 유효성은 JWT 자체 검증에 의존)
            System.err.println("Redis blacklist check error: " + e.getMessage());
            return false;
        }
    }
}