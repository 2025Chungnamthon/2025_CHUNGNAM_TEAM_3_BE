package Team3rd.DaeCar.DaeCar.domain.user.dto;

import Team3rd.DaeCar.DaeCar.domain.user.entity.User;

import java.time.LocalDateTime;

public class UserResponse {
    
    private Long id;
    private String email;
    private String phoneNumber;
    private User.Gender gender;
    private String nickname;
    private String university;
    private Boolean driverLicenseVerified;
    private LocalDateTime createdAt;
    
    public UserResponse() {}
    
    public UserResponse(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.phoneNumber = user.getPhoneNumber();
        this.gender = user.getGender();
        this.nickname = user.getNickname();
        this.university = user.getUniversity();
        this.driverLicenseVerified = user.getDriverLicenseVerified();
        this.createdAt = user.getCreatedAt();
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
    
    public String getPhoneNumber() {
        return phoneNumber;
    }
    
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    
    public User.Gender getGender() {
        return gender;
    }
    
    public void setGender(User.Gender gender) {
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
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}