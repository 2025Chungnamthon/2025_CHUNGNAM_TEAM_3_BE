package Team3rd.DaeCar.DaeCar.domain.room.dto;

import Team3rd.DaeCar.DaeCar.domain.room.entity.RoomParticipant;
import java.time.LocalDateTime;

public class RoomParticipantResponse {
    private Long id;
    private String userId;
    private LocalDateTime joinedAt;
    private Boolean isActive;

    public RoomParticipantResponse() {}

    public RoomParticipantResponse(RoomParticipant participant) {
        this.id = participant.getId();
        this.userId = participant.getUserId();
        this.joinedAt = participant.getJoinedAt();
        this.isActive = participant.getIsActive();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(LocalDateTime joinedAt) {
        this.joinedAt = joinedAt;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}