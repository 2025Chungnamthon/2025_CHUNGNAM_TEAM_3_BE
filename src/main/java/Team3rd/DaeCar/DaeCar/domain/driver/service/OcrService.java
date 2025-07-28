package Team3rd.DaeCar.DaeCar.domain.driver.service;

import Team3rd.DaeCar.DaeCar.domain.driver.dto.DriverCarnumberResponse;
import Team3rd.DaeCar.DaeCar.domain.driver.dto.DriverInfoResponse;
import Team3rd.DaeCar.DaeCar.domain.driver.entity.DriverLicense;
import Team3rd.DaeCar.DaeCar.domain.driver.repository.DriverLicenseRepository;
import Team3rd.DaeCar.DaeCar.domain.user.entity.User;
import Team3rd.DaeCar.DaeCar.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Service
@RequiredArgsConstructor
public class OcrService {

    private final DriverLicenseRepository driverLicenseRepository;
    private final UserRepository userRepository;

    // 네이버 클라우드 플랫폼에서 발급받은 정보
    private final String SECRET_KEY = "YkRpZUxCcFlmTndya2R3WnBEekJJQkR5SmhOcWhyckc=";
    private final String INVOKE_URL = "https://d3dq6wla1e.apigw.ntruss.com/custom/v1/44617/177d96c3ca5cc5ce76d43719694f88a604a7b883598789b3ddc02756840977c4/infer";

    /**
     * 운전면허 정보 추출 및 저장
     */
    @Transactional
    public DriverInfoResponse extractDriverLicenseInfo(MultipartFile file, Long userId) {
        try {
            // OCR 처리
            DriverInfoResponse info = processDriverLicenseOcr(file);

            // 유저 조회
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));

            // 기존 운전면허 정보가 있는지 확인
            Optional<DriverLicense> existingLicense = driverLicenseRepository.findByUserId(userId);

            DriverLicense license;
            if (existingLicense.isPresent()) {
                // 기존 정보 업데이트
                license = existingLicense.get();
                license.setDriverNumber(info.getDriverNumber());
                license.setDriverName(info.getDriverName());
                license.setDriverBirth(info.getDriverBirth());
                license.setDriverLicenseType(info.getDriverLicenseType());
            } else {
                // 새로운 정보 생성
                license = DriverLicense.builder()
                        .user(user)
                        .driverNumber(info.getDriverNumber())
                        .driverName(info.getDriverName())
                        .driverBirth(info.getDriverBirth())
                        .driverLicenseType(info.getDriverLicenseType())
                        .build();
            }

            // 모든 필드가 채워졌는지 확인하고 인증 상태 업데이트
            updateVerificationStatus(license);

            driverLicenseRepository.save(license);
            return info;

        } catch (Exception e) {
            throw new RuntimeException("운전면허 OCR 처리 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 차량번호 정보 추출 및 저장
     */
    @Transactional
    public DriverCarnumberResponse extractCarNumberInfo(MultipartFile file, Long userId) {
        try {
            // OCR 처리
            DriverCarnumberResponse info = processCarNumberOcr(file);

            // 유저 조회
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));

            // 운전면허 정보 조회 (없으면 생성)
            DriverLicense license = driverLicenseRepository.findByUserId(userId)
                    .orElseGet(() -> {
                        // 운전면허 정보가 없으면 기본값으로 생성
                        DriverLicense newLicense = DriverLicense.builder()
                                .user(user)
                                .driverNumber("") // 빈 값으로 초기화
                                .driverName("")
                                .driverBirth("")
                                .driverLicenseType("")
                                .driverLicenseVerified(false) // 초기값은 false
                                .build();
                        return driverLicenseRepository.save(newLicense);
                    });

            // 차량번호 업데이트
            license.setDriverCarnumber(info.getCarNumber());

            // 모든 필드가 채워졌는지 확인하고 인증 상태 업데이트
            updateVerificationStatus(license);

            driverLicenseRepository.save(license);

            return info;

        } catch (Exception e) {
            throw new RuntimeException("차량번호 OCR 처리 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 모든 필요한 필드가 채워졌는지 확인하고 인증 상태를 업데이트
     */
    private void updateVerificationStatus(DriverLicense license) {
        boolean allFieldsFilled = isNotEmptyOrNull(license.getDriverName()) &&
                isNotEmptyOrNull(license.getDriverNumber()) &&
                isNotEmptyOrNull(license.getDriverBirth()) &&
                isNotEmptyOrNull(license.getDriverLicenseType()) &&
                isNotEmptyOrNull(license.getDriverCarnumber());

        // DriverLicense 테이블 업데이트
        license.setDriverLicenseVerified(allFieldsFilled);

        // User 테이블도 함께 업데이트
        User user = license.getUser();
        user.setDriverLicenseVerified(allFieldsFilled);
        userRepository.save(user);
    }

    /**
     * 문자열이 null이거나 빈 문자열이 아닌지 확인
     */
    private boolean isNotEmptyOrNull(String str) {
        return str != null && !str.trim().isEmpty();
    }

    /**
     * 운전면허 OCR 처리 (공통 로직 분리)
     */
    private DriverInfoResponse processDriverLicenseOcr(MultipartFile file) throws Exception {
        // 1. 파일을 base64로 인코딩
        byte[] imageBytes = file.getBytes();
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);

        // 2. 파일 확장자 추출
        String fileName = file.getOriginalFilename();
        String format = "jpg";
        if (fileName != null && fileName.contains(".")) {
            format = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        }

        // 3. JSON 요청 바디 구성
        JSONObject image = new JSONObject();
        image.put("format", format);
        image.put("name", "driver_license");
        image.put("data", base64Image);

        JSONArray templateIds = new JSONArray();
        templateIds.put(38329); // driver_inform 템플릿 ID
        image.put("templateIds", templateIds);

        JSONArray images = new JSONArray();
        images.put(image);

        JSONObject requestBody = new JSONObject();
        requestBody.put("images", images);
        requestBody.put("requestId", UUID.randomUUID().toString());
        requestBody.put("version", "V2");
        requestBody.put("timestamp", System.currentTimeMillis());
        requestBody.put("lang", "ko");

        // 4. Header 구성
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-OCR-SECRET", SECRET_KEY);

        HttpEntity<String> request = new HttpEntity<>(requestBody.toString(), headers);

        // 5. 요청 전송
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.postForEntity(INVOKE_URL, request, String.class);

        // 6. 응답 처리
        String body = response.getBody();
        if (body == null || !body.trim().startsWith("{")) {
            throw new RuntimeException("OCR 응답이 JSON 형식이 아닙니다: \n" + body);
        }

        return parseDriverLicenseResult(body);
    }

    /**
     * 차량번호 OCR 처리 (공통 로직 분리)
     */
    private DriverCarnumberResponse processCarNumberOcr(MultipartFile file) throws Exception {
        // 1. 파일을 base64로 인코딩
        byte[] imageBytes = file.getBytes();
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);

        // 2. 파일 확장자 추출
        String fileName = file.getOriginalFilename();
        String format = "jpg";
        if (fileName != null && fileName.contains(".")) {
            format = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        }

        // 3. JSON 요청 바디 구성
        JSONObject image = new JSONObject();
        image.put("format", format);
        image.put("name", "car_number");
        image.put("data", base64Image);

        JSONArray templateIds = new JSONArray();
        templateIds.put(38363); // driver_car_number 템플릿 ID
        image.put("templateIds", templateIds);

        JSONArray images = new JSONArray();
        images.put(image);

        JSONObject requestBody = new JSONObject();
        requestBody.put("images", images);
        requestBody.put("requestId", UUID.randomUUID().toString());
        requestBody.put("version", "V2");
        requestBody.put("timestamp", System.currentTimeMillis());

        // 4. Header 구성
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-OCR-SECRET", SECRET_KEY);

        HttpEntity<String> request = new HttpEntity<>(requestBody.toString(), headers);

        // 5. 요청 전송
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.postForEntity(INVOKE_URL, request, String.class);

        // 6. 응답 처리
        String body = response.getBody();
        if (body == null || !body.trim().startsWith("{")) {
            throw new RuntimeException("차량번호 OCR 응답이 JSON 형식이 아닙니다: \n" + body);
        }

        return parseCarNumberResult(body);
    }

    private DriverInfoResponse parseDriverLicenseResult(String json) {
        JSONObject jsonObject = new JSONObject(json);
        JSONArray images = jsonObject.getJSONArray("images");

        if (images.length() == 0) {
            throw new RuntimeException("OCR 결과가 없습니다.");
        }

        JSONObject firstImage = images.getJSONObject(0);
        JSONArray fields = firstImage.getJSONArray("fields");

        String driverNumber = "", driverName = "", driverBirth = "", driverLicenseType = "";

        for (int i = 0; i < fields.length(); i++) {
            JSONObject field = fields.getJSONObject(i);
            String name = field.getString("name");
            String inferText = field.getString("inferText");

            switch (name) {
                case "driver_number": driverNumber = inferText; break;
                case "driver_name": driverName = inferText; break;
                case "driver_birth": driverBirth = inferText; break;
                case "driver_licenseType": driverLicenseType = inferText; break;
            }
        }

        return DriverInfoResponse.builder()
                .driverNumber(driverNumber)
                .driverName(driverName)
                .driverBirth(driverBirth)
                .driverLicenseType(driverLicenseType)
                .build();
    }

    private DriverCarnumberResponse parseCarNumberResult(String json) {
        JSONObject jsonObject = new JSONObject(json);
        JSONArray images = jsonObject.getJSONArray("images");

        if (images.length() == 0) {
            throw new RuntimeException("차량번호 OCR 결과가 없습니다.");
        }

        JSONObject firstImage = images.getJSONObject(0);
        JSONArray fields = firstImage.getJSONArray("fields");

        String carNumber = "";

        for (int i = 0; i < fields.length(); i++) {
            JSONObject field = fields.getJSONObject(i);
            String name = field.getString("name");
            String inferText = field.getString("inferText");

            switch (name) {
                case "car_number": carNumber = inferText; break;
            }
        }

        return DriverCarnumberResponse.builder()
                .carNumber(carNumber)
                .build();
    }
}