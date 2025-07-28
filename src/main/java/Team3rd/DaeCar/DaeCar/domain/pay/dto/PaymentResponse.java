package Team3rd.DaeCar.DaeCar.domain.pay.dto;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
//결제 완료 응답 DTO
public class PaymentResponse {

    private boolean success;
    private String message;
    private PaymentData data;

    public PaymentResponse() {}

    public PaymentResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public PaymentResponse(boolean success, String message, PaymentData data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    // 정적 팩토리 메서드
    public static PaymentResponse success(String message) {
        return new PaymentResponse(true, message);
    }

    public static PaymentResponse success(String message, PaymentData data) {
        return new PaymentResponse(true, message, data);
    }

    public static PaymentResponse failure(String message) {
        return new PaymentResponse(false, message);
    }

public static class PaymentData {
        private Long roomId;
    private int totalParticipants;
    private int successfulPayments;
    private String totalAmount;

    public PaymentData() {}

    public PaymentData(Long roomId, int totalParticipants, int successfulPayments, String totalAmount) {
        this.roomId = roomId;
        this.totalParticipants = totalParticipants;
        this.successfulPayments = successfulPayments;
        this.totalAmount = totalAmount;
    }
}

}


