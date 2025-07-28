package Team3rd.DaeCar.DaeCar.domain.pay.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
//도착 완료 요청 DTO
public class CompleteTripRequest {
    @NotNull(message = "카풀방 ID는 필수입니다.")
    private Long roomId;

    @NotNull(message = "운전자 ID는 필수입니다.")
    private Long driverUserId;





}
