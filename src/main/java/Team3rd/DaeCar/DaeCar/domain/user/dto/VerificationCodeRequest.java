package Team3rd.DaeCar.DaeCar.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class VerificationCodeRequest {
    
    @NotBlank(message = "인증코드는 필수입니다.")
    @Pattern(regexp = "^\\d{6}$", message = "인증코드는 6자리 숫자여야 합니다.")
    private String verificationCode;
    
    public String getVerificationCode() {
        return verificationCode;
    }
    
    public void setVerificationCode(String verificationCode) {
        this.verificationCode = verificationCode;
    }
}