package Team3rd.DaeCar.DaeCar.domain.pay.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_transactions")
public class PaymentTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "room_id", nullable = false)
    private Long roomId;

    @Column(name = "from_user_id", nullable = false)
    private Long fromUserId; // 결제하는 사람 (승객)

    @Column(name = "to_user_id", nullable = false)
    private Long toUserId;   // 받는 사람 (운전자)

    @Column(name = "amount", nullable = false, precision = 10, scale = 0)
    private BigDecimal amount;//결제 금액 저장

    @Column(name = "description")
    private String description; // 결제 설명 (예: "강남역 → 홍대입구 카풀비")

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TransactionStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;//결제 거래가 언제 생성되었는지 기록

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;// 결제 상태가 마지막으로 언제 변경되었는지 기록

    // 실패 시 오류 메시지
    @Column(name = "error_message")
    private String errorMessage;

    public enum TransactionStatus {
        SUCCESS("성공"),
        FAILED("실패"),
        PENDING("처리중");

        private final String description;

        TransactionStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Constructors
    public PaymentTransaction() {}

    public PaymentTransaction(Long roomId, Long fromUserId, Long toUserId,
                              BigDecimal amount, String description) {
        this.roomId = roomId;
        this.fromUserId = fromUserId;
        this.toUserId = toUserId;
        this.amount = amount;
        this.description = description;
        this.status = TransactionStatus.PENDING;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public Long getFromUserId() {
        return fromUserId;
    }

    public void setFromUserId(Long fromUserId) {
        this.fromUserId = fromUserId;
    }

    public Long getToUserId() {
        return toUserId;
    }

    public void setToUserId(Long toUserId) {
        this.toUserId = toUserId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    // 편의 메서드들
    public boolean isSuccess() {
        return TransactionStatus.SUCCESS.equals(this.status);
    }

    public boolean isFailed() {
        return TransactionStatus.FAILED.equals(this.status);
    }

    public boolean isPending() {
        return TransactionStatus.PENDING.equals(this.status);
    }

    public void markAsSuccess() {
        this.status = TransactionStatus.SUCCESS;
        this.errorMessage = null;
    }

    public void markAsFailed(String errorMessage) {
        this.status = TransactionStatus.FAILED;
        this.errorMessage = errorMessage;
    }
}
