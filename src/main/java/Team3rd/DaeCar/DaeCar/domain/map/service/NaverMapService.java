package Team3rd.DaeCar.DaeCar.domain.map.service;

import Team3rd.DaeCar.DaeCar.domain.map.dto.RouteResponse;
import Team3rd.DaeCar.DaeCar.global.config.NaverMapConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;

@Service
public class NaverMapService {
    @Autowired
    private NaverMapConfig naverMapConfig;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String testConfig() {
        return "Client ID: " + naverMapConfig.getClientId() +
                " / Client Secret: " + naverMapConfig.getClientSecret();
    }

    public RouteResponse calculateRoute(double startLat, double startLng, double endLat, double endLng) {
        try {
            String start = startLng + "," + startLat;
            String goal = endLng + "," + endLat;

            String url = UriComponentsBuilder
                    .fromHttpUrl("https://maps.apigw.ntruss.com/map-direction/v1/driving")
                    .queryParam("start", start)
                    .queryParam("goal", goal)
                    .queryParam("option", "trafast")
                    .toUriString();

            HttpHeaders headers = new HttpHeaders();
            headers.set("X-NCP-APIGW-API-KEY-ID", naverMapConfig.getClientId());
            headers.set("X-NCP-APIGW-API-KEY", naverMapConfig.getClientSecret());

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, String.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode route = root.path("route");

                if (route.path("trafast").isArray() && route.path("trafast").size() > 0) {
                    JsonNode trafast = route.path("trafast").get(0);
                    JsonNode summary = trafast.path("summary");

                    // 데이터 추출
                    Integer distance = summary.path("distance").asInt();
                    Integer duration = summary.path("duration").asInt();
                    Integer tollFare = summary.path("tollFare").asInt();
                    Integer fuelPrice = summary.path("fuelPrice").asInt();
                    Integer taxiFare = summary.path("taxiFare").asInt();

                    // 경로 좌표 추출
                    List<List<Double>> path = new ArrayList<>();
                    JsonNode pathArray = trafast.path("path");

                    for (JsonNode point : pathArray) {
                        List<Double> coord = new ArrayList<>();
                        coord.add(point.get(0).asDouble()); // 경도
                        coord.add(point.get(1).asDouble()); // 위도
                        path.add(coord);
                    }

                    // 결과 생성
                    RouteResponse result = new RouteResponse();
                    result.setDistance(distance);
                    result.setDuration(duration);
                    result.setTollFare(tollFare);
                    result.setFuelPrice(fuelPrice);
                    result.setTaxiFare(taxiFare);
                    result.setPath(path);
                    result.setSuccess(true);

                    return result;
                } else {
                    return RouteResponse.error("경로를 찾을 수 없습니다.");
                }

            } else {
                return RouteResponse.error("API 호출 실패: " + response.getStatusCode());
            }

        } catch (Exception e) {
            return RouteResponse.error("경로 계산 오류: " + e.getMessage());
        }
    }

}


