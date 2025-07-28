package Team3rd.DaeCar.DaeCar.domain.pay.dto;


import Team3rd.DaeCar.DaeCar.domain.pay.entity.PaymentTransaction;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
@NoArgsConstructor
//결제 내역 조회 응답 DTO
public class TransactionHistoryResponse {
    private Long transactionId;
    private Long roomId;
    private String fromUserNickname;
    private String toUserNickname;
    private BigDecimal amount;
    private String formattedAmount;
    private String description;
    private String status;
    private String statusDescription;
    private LocalDateTime createdAt;
    private String formattedCreatedAt;
    private String errorMessage;


    public TransactionHistoryResponse(PaymentTransaction transaction,
                                      String fromUserNickname, String toUserNickname) {
        this.transactionId = transaction.getId();
        this.roomId = transaction.getRoomId();
        this.fromUserNickname = fromUserNickname;
        this.toUserNickname = toUserNickname;
        this.amount = transaction.getAmount();
        this.formattedAmount = String.format("%,d원", transaction.getAmount().longValue());
        this.description = transaction.getDescription();
        this.status = transaction.getStatus().name();
        this.statusDescription = transaction.getStatus().getDescription();
        this.createdAt = transaction.getCreatedAt();
        this.formattedCreatedAt = transaction.getCreatedAt()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        this.errorMessage = transaction.getErrorMessage();
    }
}
