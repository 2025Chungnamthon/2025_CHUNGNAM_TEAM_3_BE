package Team3rd.DaeCar.DaeCar.domain.driver.entity;

import Team3rd.DaeCar.DaeCar.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

@Table(name = "driver_license")
public class DriverLicense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    private String driverNumber;          // 면허 번호
    private String driverName;            // 이름
    private String driverBirth;           // 생년월일
    private String driverLicenseType;     // 면허 종류

    private String driverCarnumber; //차량 번호


    @Column(name = "driver_license_verified", nullable = false)
    @Builder.Default
    private Boolean driverLicenseVerified = false;


}
