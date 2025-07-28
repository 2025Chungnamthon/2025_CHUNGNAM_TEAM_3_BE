package Team3rd.DaeCar.DaeCar.domain.room.entity;

import Team3rd.DaeCar.DaeCar.domain.user.entity.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "room_participants")
public class RoomParticipant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "room_id", nullable = false)
    private Long roomId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    // 역할 구분 필드 추가
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private ParticipantRole role;

    public enum ParticipantRole {
        CREATOR,    // 방장 (카풀 요청자)
        DRIVER,     // 운전자
        PASSENGER   // 일반 탑승자
    }

    // 결제 완료 여부 필드 추가
    @Column(name = "is_paid", nullable = false)
    private Boolean isPaid = false;

    @Column(name = "joined_at")
    private LocalDateTime joinedAt;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @PrePersist
    protected void onCreate() {
        joinedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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


    public ParticipantRole getRole() {
        return role;
    }

    public void setRole(ParticipantRole role) {
        this.role = role;
    }

    public Boolean getIsPaid() {
        return isPaid;
    }

    public void setIsPaid(Boolean isPaid) {
        this.isPaid = isPaid;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getRoomId() { return roomId; }
    public void setRoomId(Long roomId) { this.roomId = roomId; }

    // 편의 메서드들
    public boolean isDriver() {
        return ParticipantRole.DRIVER.equals(this.role);
    }

    public boolean isCreator() {
        return ParticipantRole.CREATOR.equals(this.role);
    }

    public boolean isPassenger() {
        return ParticipantRole.PASSENGER.equals(this.role);
    }

    public boolean needsToPay() {
        return !isDriver() && !isPaid;
    }
}