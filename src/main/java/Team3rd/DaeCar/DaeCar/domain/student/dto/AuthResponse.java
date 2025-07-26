package Team3rd.DaeCar.DaeCar.domain.student.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthResponse {
    private String email;
    private String school;
    private String gender;
    private String Nickname;



    public AuthResponse(String email, String school, String gender, String Nickname) {
        this.email = email;
        this.school = school;
        this.gender = gender;
        this.Nickname = Nickname;

    }


}