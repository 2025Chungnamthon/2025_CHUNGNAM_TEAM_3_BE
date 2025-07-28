package Team3rd.DaeCar.DaeCar.domain.room.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "room_join_requests")
public class RoomJoinRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "room_id", nullable = false)
    private Long roomId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private RequestStatus status = RequestStatus.PENDING;

    @Column(name = "message", columnDefinition = "TEXT")
    private String message;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "processed_by")
    private Long processedBy; // 승인/거절한 사용자 ID (방장)

    public enum RequestStatus {
        PENDING,    // 대기 중
        APPROVED,   // 승인됨
        REJECTED    // 거절됨
    }

    @PrePersist
    protected void onCreate() {
        requestedAt = LocalDateTime.now();
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

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public RequestStatus getStatus() {
        return status;
    }

    public void setStatus(RequestStatus status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public LocalDateTime getRequestedAt() {
        return requestedAt;
    }

    public void setRequestedAt(LocalDateTime requestedAt) {
        this.requestedAt = requestedAt;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }

    public Long getProcessedBy() {
        return processedBy;
    }

    public void setProcessedBy(Long processedBy) {
        this.processedBy = processedBy;
    }

    // 편의 메서드들
    public boolean isPending() {
        return RequestStatus.PENDING.equals(this.status);
    }

    public boolean isApproved() {
        return RequestStatus.APPROVED.equals(this.status);
    }

    public boolean isRejected() {
        return RequestStatus.REJECTED.equals(this.status);
    }

    public void approve(Long approvedBy) {
        this.status = RequestStatus.APPROVED;
        this.processedBy = approvedBy;
        this.processedAt = LocalDateTime.now();
    }

    public void reject(Long rejectedBy, String reason) {
        this.status = RequestStatus.REJECTED;
        this.processedBy = rejectedBy;
        this.rejectionReason = reason;
        this.processedAt = LocalDateTime.now();
    }
}