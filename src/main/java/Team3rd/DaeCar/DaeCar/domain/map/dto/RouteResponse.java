package Team3rd.DaeCar.DaeCar.domain.map.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RouteResponse {
    private Integer distance;        // 거리 (미터)
    private Integer duration;        // 소요 시간 (밀리초)
    private Integer tollFare;        // 통행료
    private Integer fuelPrice;       // 연료비
    private Integer taxiFare;        // 택시요금
    private List<List<Double>> path; // 경로 좌표들
    private boolean success;
    private String errorMessage;

    // 편의 메소드들
    public Double getDistanceInKm() {
        return distance != null ? distance / 1000.0 : null;
    }

    public Integer getDurationInMinutes() {
        return duration != null ? duration / 60000 : null;
    }

    public Integer gettexiCost() {
        return taxiFare;
    }

    public static RouteResponse error(String errorMessage) {
        RouteResponse response = new RouteResponse();
        response.success = false;
        response.errorMessage = errorMessage;
        return response;
    }
}