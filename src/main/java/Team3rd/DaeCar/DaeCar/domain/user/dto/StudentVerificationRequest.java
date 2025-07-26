package Team3rd.DaeCar.DaeCar.domain.user.dto;

import jakarta.validation.constraints.NotBlank;

public class StudentVerificationRequest {
    
    @NotBlank(message = "학생증 이미지는 필수입니다.")
    private String studentCardImageUrl;
    
    @NotBlank(message = "대학교명은 필수입니다.")
    private String university;
    
    public String getStudentCardImageUrl() {
        return studentCardImageUrl;
    }
    
    public void setStudentCardImageUrl(String studentCardImageUrl) {
        this.studentCardImageUrl = studentCardImageUrl;
    }
    
    public String getUniversity() {
        return university;
    }
    
    public void setUniversity(String university) {
        this.university = university;
    }
}