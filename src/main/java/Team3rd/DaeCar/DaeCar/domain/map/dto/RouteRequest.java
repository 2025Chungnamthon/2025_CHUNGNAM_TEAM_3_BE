package Team3rd.DaeCar.DaeCar.domain.map.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RouteRequest {
    @NotNull(message = "출발지 위도는 필수입니다")
    private Double startLatitude;

    @NotNull(message = "출발지 경도는 필수입니다")
    private Double startLongitude;

    @NotNull(message = "도착지 위도는 필수입니다")
    private Double endLatitude;

    @NotNull(message = "도착지 경도는 필수입니다")
    private Double endLongitude;
}
