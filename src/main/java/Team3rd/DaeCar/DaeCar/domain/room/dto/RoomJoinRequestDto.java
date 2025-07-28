package Team3rd.DaeCar.DaeCar.domain.room.dto;

import Team3rd.DaeCar.DaeCar.domain.room.entity.RoomJoinRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public class RoomJoinRequestDto {
    
    // 참여 요청 생성 DTO
    public static class CreateRequest {
        @NotNull(message = "방 ID는 필수입니다")
        private Long roomId;
        
        @NotNull(message = "사용자 ID는 필수입니다")
        private Long userId;
        
        private String message;

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

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
    
    // 참여 요청 처리 DTO
    public static class ProcessRequest {
        @NotNull(message = "요청 ID는 필수입니다")
        private Long requestId;
        
        @NotNull(message = "처리자 ID는 필수입니다")
        private Long processedBy;
        
        @NotNull(message = "승인 여부는 필수입니다")
        private Boolean approved;
        
        private String rejectionReason;

        public Long getRequestId() {
            return requestId;
        }

        public void setRequestId(Long requestId) {
            this.requestId = requestId;
        }

        public Long getProcessedBy() {
            return processedBy;
        }

        public void setProcessedBy(Long processedBy) {
            this.processedBy = processedBy;
        }

        public Boolean getApproved() {
            return approved;
        }

        public void setApproved(Boolean approved) {
            this.approved = approved;
        }

        public String getRejectionReason() {
            return rejectionReason;
        }

        public void setRejectionReason(String rejectionReason) {
            this.rejectionReason = rejectionReason;
        }
    }
    
    // 참여 요청 응답 DTO
    public static class Response {
        private Long id;
        private Long roomId;
        private Long userId;
        private RoomJoinRequest.RequestStatus status;
        private String message;
        private String rejectionReason;
        private LocalDateTime requestedAt;
        private LocalDateTime processedAt;
        private Long processedBy;

        public Response() {}

        public Response(RoomJoinRequest request) {
            this.id = request.getId();
            this.roomId = request.getRoomId();
            this.userId = request.getUserId();
            this.status = request.getStatus();
            this.message = request.getMessage();
            this.rejectionReason = request.getRejectionReason();
            this.requestedAt = request.getRequestedAt();
            this.processedAt = request.getProcessedAt();
            this.processedBy = request.getProcessedBy();
        }

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

        public RoomJoinRequest.RequestStatus getStatus() {
            return status;
        }

        public void setStatus(RoomJoinRequest.RequestStatus status) {
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
    }
}