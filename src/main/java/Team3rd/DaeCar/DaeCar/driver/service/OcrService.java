package Team3rd.DaeCar.DaeCar.driver.service;

import Team3rd.DaeCar.DaeCar.driver.dto.DriverInfoResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

@Service
public class OcrService {

    // 네이버 클라우드 플랫폼에서 발급받은 정보
    private final String SECRET_KEY = "YkRpZUxCcFlmTndya2R3WnBEekJJQkR5SmhOcWhyckc=";
    private final String INVOKE_URL = "https://d3dq6wla1e.apigw.ntruss.com/custom/v1/44617/177d96c3ca5cc5ce76d43719694f88a604a7b883598789b3ddc02756840977c4/infer";

    public DriverInfoResponse extractDriverLicenseInfo(MultipartFile file) {
        try {
            // 1. 파일을 base64로 인코딩
            byte[] imageBytes = file.getBytes();
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);

            // 2. 파일 확장자 추출
            String fileName = file.getOriginalFilename();
            String format = "jpg"; // 기본값
            if (fileName != null && fileName.contains(".")) {
                format = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
            }

            // 3. JSON 요청 바디 구성 (템플릿 OCR 형식)
            JSONObject image = new JSONObject();
            image.put("format", format);
            image.put("name", "driver_license");
            image.put("data", base64Image);

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

            System.out.println("OCR 응답 본문: ");
            System.out.println(response.getBody());

            String body = response.getBody();
            if (body == null || !body.trim().startsWith("{")) {
                throw new RuntimeException("OCR 응답이 JSON 형식이 아닙니다: \n" + body);
            }

            return parseOcrResult(body);

        } catch (Exception e) {
            throw new RuntimeException("OCR 처리 실패: " + e.getMessage(), e);
        }
    }

    private DriverInfoResponse parseOcrResult(String json) {
        JSONObject jsonObject = new JSONObject(json);
        JSONArray images = jsonObject.getJSONArray("images");

        if (images.length() == 0) {
            throw new RuntimeException("OCR 결과가 없습니다.");
        }

        JSONObject firstImage = images.getJSONObject(0);

        // 템플릿 OCR의 경우 fields 배열에서 데이터 추출
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
}

