package Team3rd.DaeCar.DaeCar.domain.user.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;

@Entity
@Table(name = "users")
public class User implements UserDetails {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Email
    @NotBlank
    @Column(unique = true, nullable = false)
    private String email;
    
    @NotBlank
    @Column(nullable = false)
    private String password;
    
    @Pattern(regexp = "^010-\\d{4}-\\d{4}$", message = "휴대전화 형식이 올바르지 않습니다.")
    @Column(name = "phone_number")
    private String phoneNumber;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;
    
    @NotBlank
    @Column(nullable = false)
    private String nickname;
    
    @NotBlank
    @Column(nullable = false)
    private String university;
    
    @Column(name = "driver_license_verified", nullable = false)
    private Boolean driverLicenseVerified = false;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "student_verification_status", nullable = false)
    private StudentVerificationStatus studentVerificationStatus = StudentVerificationStatus.PENDING;
    
    @Column(name = "student_email")
    private String studentEmail;
    
    @Column(name = "student_email_verified", nullable = false)
    private Boolean studentEmailVerified = false;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    public enum Gender {
        MALE, FEMALE
    }
    
    public enum StudentVerificationStatus {
        PENDING,
        VERIFIED,
        REJECTED
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getPhoneNumber() {
        return phoneNumber;
    }
    
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    
    public Gender getGender() {
        return gender;
    }
    
    public void setGender(Gender gender) {
        this.gender = gender;
    }
    
    public String getNickname() {
        return nickname;
    }
    
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
    
    public String getUniversity() {
        return university;
    }
    
    public void setUniversity(String university) {
        this.university = university;
    }
    
    public Boolean getDriverLicenseVerified() {
        return driverLicenseVerified;
    }
    
    public void setDriverLicenseVerified(Boolean driverLicenseVerified) {
        this.driverLicenseVerified = driverLicenseVerified;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    public StudentVerificationStatus getStudentVerificationStatus() {
        return studentVerificationStatus;
    }
    
    public void setStudentVerificationStatus(StudentVerificationStatus studentVerificationStatus) {
        this.studentVerificationStatus = studentVerificationStatus;
    }
    
    public String getStudentEmail() {
        return studentEmail;
    }
    
    public void setStudentEmail(String studentEmail) {
        this.studentEmail = studentEmail;
    }
    
    public Boolean getStudentEmailVerified() {
        return studentEmailVerified;
    }
    
    public void setStudentEmailVerified(Boolean studentEmailVerified) {
        this.studentEmailVerified = studentEmailVerified;
    }
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList();
    }
    
    @Override
    public String getUsername() {
        return email;
    }
    
    @Override
    public boolean isAccountNonExpired() {
        return isActive;
    }
    
    @Override
    public boolean isAccountNonLocked() {
        return isActive;
    }
    
    @Override
    public boolean isCredentialsNonExpired() {
        return isActive;
    }
    
    @Override
    public boolean isEnabled() {
        return isActive;
    }
}