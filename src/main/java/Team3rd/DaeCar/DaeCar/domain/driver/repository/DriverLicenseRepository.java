package Team3rd.DaeCar.DaeCar.domain.driver.repository;


import Team3rd.DaeCar.DaeCar.domain.driver.entity.DriverLicense;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DriverLicenseRepository extends JpaRepository<DriverLicense, Long> {
    Optional<DriverLicense> findByUserId(Long userId);
}