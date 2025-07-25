package Team3rd.DaeCar.DaeCar.domain.room.dto;

import Team3rd.DaeCar.DaeCar.domain.room.entity.Room;

import java.time.LocalDateTime;

public class RoomResponse {
    private Long id;
    private String name;
    private Integer maxParticipants;
    private Integer currentParticipants;
    private LocalDateTime createdAt;
    private Boolean isActive;

    public RoomResponse() {}

    public RoomResponse(Room room) {
        this.id = room.getId();
        this.name = room.getName();
        this.maxParticipants = room.getMaxParticipants();
        this.currentParticipants = room.getCurrentParticipants();
        this.createdAt = room.getCreatedAt();
        this.isActive = room.getIsActive();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getMaxParticipants() {
        return maxParticipants;
    }

    public void setMaxParticipants(Integer maxParticipants) {
        this.maxParticipants = maxParticipants;
    }

    public Integer getCurrentParticipants() {
        return currentParticipants;
    }

    public void setCurrentParticipants(Integer currentParticipants) {
        this.currentParticipants = currentParticipants;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}