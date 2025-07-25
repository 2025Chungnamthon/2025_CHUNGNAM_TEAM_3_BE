package Team3rd.DaeCar.DaeCar.domain.room.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class JoinRoomRequest {
    @NotNull(message = "방 ID는 필수입니다")
    private Long roomId;

    @NotBlank(message = "사용자 ID는 필수입니다")
    private String userId;

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}