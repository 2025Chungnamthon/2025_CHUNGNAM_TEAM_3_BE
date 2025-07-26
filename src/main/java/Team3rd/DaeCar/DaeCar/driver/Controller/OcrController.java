package Team3rd.DaeCar.DaeCar.driver.Controller;

import Team3rd.DaeCar.DaeCar.driver.dto.DriverInfoResponse;
import Team3rd.DaeCar.DaeCar.driver.service.OcrService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@RestController
@RequestMapping("/api/driver")
@RequiredArgsConstructor
public class OcrController {

    private final OcrService ocrService;


    @PostMapping("/license")
    public ResponseEntity<DriverInfoResponse> extractLicense(
            @RequestParam("file") MultipartFile file
    ) {
        DriverInfoResponse dto = ocrService.extractDriverLicenseInfo(file);
        return ResponseEntity.ok(dto);
    }
}