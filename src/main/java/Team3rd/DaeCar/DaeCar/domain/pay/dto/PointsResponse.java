package Team3rd.DaeCar.DaeCar.domain.pay.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
//포인트 조회 응답 DTO

public class PointsResponse {

    private Long userId;
    private String nickname;
    private BigDecimal currentPoints;
    private String formattedPoints; // "10,000원" 형식


    public PointsResponse(Long userId, String nickname, BigDecimal currentPoints) {
        this.userId = userId;
        this.nickname = nickname;
        this.currentPoints = currentPoints;
        this.formattedPoints = String.format("%,d원", currentPoints.longValue());
    }

    public void setCurrentPoints(BigDecimal currentPoints) {
        this.currentPoints = currentPoints;
        this.formattedPoints = String.format("%,d원", currentPoints.longValue());
    }
}
