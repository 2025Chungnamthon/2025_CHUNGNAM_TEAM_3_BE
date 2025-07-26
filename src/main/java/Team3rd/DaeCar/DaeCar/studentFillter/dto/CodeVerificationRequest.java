package Team3rd.DaeCar.DaeCar.studentFillter.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CodeVerificationRequest {

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String code;

}
