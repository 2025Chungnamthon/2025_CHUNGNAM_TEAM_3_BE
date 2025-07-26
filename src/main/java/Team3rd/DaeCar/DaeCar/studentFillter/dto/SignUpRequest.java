package Team3rd.DaeCar.DaeCar.studentFillter.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignUpRequest {
    @NotBlank
    //private String email;

    @NotBlank
    private String nickname;

    @NotBlank
    private String gender;
}
