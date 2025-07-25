package Team3rd.DaeCar.DaeCar.domain.room.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;

public class CreateRoomRequest {
    @NotBlank(message = "방 이름은 필수입니다")
    private String name;

    @NotNull(message = "최대 참여자 수는 필수입니다")
    @Min(value = 1, message = "최대 참여자 수는 1명 이상이어야 합니다")
    private Integer maxParticipants;

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
}