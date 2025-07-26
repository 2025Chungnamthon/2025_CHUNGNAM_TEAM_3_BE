package Team3rd.DaeCar.DaeCar.studentFillter.dto;

import Team3rd.DaeCar.DaeCar.studentFillter.utill.SchoolDomainMapper;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;


public class EmailRequest {
    @Email
    @NotBlank
    private String email;
    public @Email String getEmail() {
        return email;
    }

    public void setEmail(@Email String email) {
        if (!email.endsWith(".ac.kr")) {
            throw new IllegalArgumentException("대학 이메일만 허용됩니다.");
        }
        this.email = email;
    }
}
