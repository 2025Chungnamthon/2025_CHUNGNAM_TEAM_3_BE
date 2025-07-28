package Team3rd.DaeCar.DaeCar.domain.room.dto;

import Team3rd.DaeCar.DaeCar.domain.room.enums.RoomType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateRoomRequest {

    @NotNull(message = "사용자 ID는 필수입니다")
    private Long userId;


    @NotBlank(message = "방 이름은 필수입니다")
    private String name; // 방 이름

    @NotNull(message = "최대 참여자 수는 필수입니다")
    @Min(value = 1, message = "최대 참여자 수는 1명 이상이어야 합니다")
    private Integer maxParticipants; // 최대 참여자 수

    @NotNull(message = "방 타입은 필수입니다")
    private RoomType roomType; // 방 타입

    @NotBlank(message = "출발지는 필수입니다")
    private String departureLocation; // 출발지 주소

    @NotBlank(message = "목적지는 필수입니다")
    private String destination; // 도착지 주소

    @NotNull(message = "출발 시간은 필수입니다")

    private LocalDateTime departureTime; // 출발 시간

    private BigDecimal costPerPerson;

    private String description;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
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

    public RoomType getRoomType() {
        return roomType;
    }

    public void setRoomType(RoomType roomType) {
        this.roomType = roomType;
    }

    public String getDepartureLocation() {
        return departureLocation;
    }

    public void setDepartureLocation(String departureLocation) {
        this.departureLocation = departureLocation;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }



    // === 좌표 정보 (새로 추가!) ===
    @NotNull(message = "출발지 위도는 필수입니다")
    private Double startLatitude; // 출발지 위도

    @NotNull(message = "출발지 경도는 필수입니다")
    private Double startLongitude; // 출발지 경도

    @NotNull(message = "도착지 위도는 필수입니다")
    private Double endLatitude; // 도착지 위도

    @NotNull(message = "도착지 경도는 필수입니다")
    private Double endLongitude; // 도착지 경도
}