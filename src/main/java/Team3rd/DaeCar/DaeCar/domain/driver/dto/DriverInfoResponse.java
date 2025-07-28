package Team3rd.DaeCar.DaeCar.domain.driver.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DriverInfoResponse {
    private String driverNumber;
    private String driverName;
    private String driverBirth;
    private String driverLicenseType;
}
