package Team3rd.DaeCar.DaeCar.domain.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class StudentEmailVerificationRequest {
    
    @Email(message = "올바른 이메일 형식이어야 합니다.")
    @NotBlank(message = "학생 이메일은 필수입니다.")
    private String studentEmail;
    
    public String getStudentEmail() {
        return studentEmail;
    }
    
    public void setStudentEmail(String studentEmail) {
        this.studentEmail = studentEmail;
    }
}