package Team3rd.DaeCar.DaeCar.domain.room.entity;

import jakarta.persistence.*;
import Team3rd.DaeCar.DaeCar.domain.room.enums.RoomType;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "rooms")
@Getter
@Setter
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 방 고유 번호

    @Column(nullable = false)
    private String name; // 방 이름

    @Column(name = "max_participants")
    private Integer maxParticipants; // 최대 참여자 수

    @Column(name = "current_participants")
    private Integer currentParticipants = 0; // 현재 참여자 수

    @Column(name = "created_at")
    private LocalDateTime createdAt; // 방 생성 시간

    @Column(name = "updated_at")
    private LocalDateTime updatedAt; // 방 수정 시간

    @Column(name = "is_active")
    private Boolean isActive = true; // 방 활성화 여부

    @Enumerated(EnumType.STRING)
    @Column(name = "room_type")
    private RoomType roomType; // 방 타입 (카풀 등)

    @Column(name = "departure_location")
    private String departureLocation; // 출발지 주소

    @Column(name = "destination")
    private String destination; // 도착지 주소

    @Column(name = "departure_time")
    private LocalDateTime departureTime; // 출발 시간

    @Column(name = "cost_per_person", precision = 10, scale = 0)
    private BigDecimal costPerPerson; // 1인당 비용

    @Column(name = "total_cost", precision = 10, scale = 0)
    private BigDecimal totalCost; // 총 비용

    @Column(name = "description", columnDefinition = "TEXT")
    private String description; // 방 설명

    @Column(name = "driver_user_id")
    private Long driverUserId; // 운전자 사용자 ID

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private RoomStatus status = RoomStatus.WAITING; // 방 상태

    // === 좌표 및 경로 정보 ===
    @Column(name = "start_latitude", precision = 10, scale = 8)
    private BigDecimal startLatitude; // 출발지 위도

    @Column(name = "start_longitude", precision = 11, scale = 8)
    private BigDecimal startLongitude; // 출발지 경도

    @Column(name = "end_latitude", precision = 10, scale = 8)
    private BigDecimal endLatitude; // 도착지 위도

    @Column(name = "end_longitude", precision = 11, scale = 8)
    private BigDecimal endLongitude; // 도착지 경도

    @Column(name = "estimated_distance")
    private Integer estimatedDistance; // 예상 거리 (미터)

    @Column(name = "estimated_duration")
    private Integer estimatedDuration; // 예상 소요시간 (밀리초)

    @Column(name = "estimated_taxi_fare")
    private Integer estimatedTaxiFare; // 예상 택시요금 (원)

    @Column(name = "route_path", columnDefinition = "TEXT")
    private String routePath; // 경로 좌표 데이터 (JSON 형태)

    public enum RoomStatus {
        WAITING,    // 대기중
        MATCHED,    // 매칭 완료
        DRIVING,    // 운행중
        COMPLETED,  // 완료
        CANCELLED   // 취소
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
}