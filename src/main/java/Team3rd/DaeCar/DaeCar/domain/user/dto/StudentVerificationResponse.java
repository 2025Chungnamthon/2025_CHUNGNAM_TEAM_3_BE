package Team3rd.DaeCar.DaeCar.domain.user.dto;

import Team3rd.DaeCar.DaeCar.domain.user.entity.User.StudentVerificationStatus;

public class StudentVerificationResponse {
    
    private Long userId;
    private StudentVerificationStatus status;
    private String message;
    
    public StudentVerificationResponse(Long userId, StudentVerificationStatus status, String message) {
        this.userId = userId;
        this.status = status;
        this.message = message;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public StudentVerificationStatus getStatus() {
        return status;
    }
    
    public void setStatus(StudentVerificationStatus status) {
        this.status = status;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
}