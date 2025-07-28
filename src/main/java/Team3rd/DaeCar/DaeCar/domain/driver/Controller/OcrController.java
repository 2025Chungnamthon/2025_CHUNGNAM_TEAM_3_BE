package Team3rd.DaeCar.DaeCar.domain.driver.Controller;

import Team3rd.DaeCar.DaeCar.domain.driver.dto.DriverCarnumberResponse;
import Team3rd.DaeCar.DaeCar.domain.driver.dto.DriverInfoResponse;
import Team3rd.DaeCar.DaeCar.domain.driver.service.OcrService;
import Team3rd.DaeCar.DaeCar.domain.user.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/driver")
@RequiredArgsConstructor
public class OcrController {

    private final OcrService ocrService;


    @PostMapping("/license")
    public ResponseEntity<DriverInfoResponse> extractLicense(
            @RequestParam("file") MultipartFile file,
            @RequestParam("userId") Long userId//나중에 jwt토큰으로
    ) {

        DriverInfoResponse dto = ocrService.extractDriverLicenseInfo(file, userId);
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/carnumber")
    public ResponseEntity<DriverCarnumberResponse> extractCarNumber(
            @RequestParam("file") MultipartFile file,
            @RequestParam("userId") Long userId
    ) {
        DriverCarnumberResponse dto = ocrService.extractCarNumberInfo(file, userId);
        return ResponseEntity.ok(dto);
    }
}