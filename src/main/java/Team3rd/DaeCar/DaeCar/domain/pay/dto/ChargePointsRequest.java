package Team3rd.DaeCar.DaeCar.domain.pay.dto;


import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
//포인트 충전 요청 DTO
public class ChargePointsRequest {
    @NotNull(message = "사용자 ID는 필수입니다.")
    private Long userId;

    @NotNull(message = "충전 금액은 필수입니다.")
    @DecimalMin(value = "1000", message = "최소 1,000원 이상 충전해야 합니다.")
    private BigDecimal amount;
}
