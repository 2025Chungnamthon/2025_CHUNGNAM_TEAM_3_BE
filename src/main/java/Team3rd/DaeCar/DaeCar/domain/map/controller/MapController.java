package Team3rd.DaeCar.DaeCar.domain.map.controller;

import Team3rd.DaeCar.DaeCar.domain.map.dto.RouteRequest;
import Team3rd.DaeCar.DaeCar.domain.map.dto.RouteResponse;
import Team3rd.DaeCar.DaeCar.domain.map.service.NaverMapService;
import Team3rd.DaeCar.DaeCar.domain.room.entity.Room;
import Team3rd.DaeCar.DaeCar.domain.room.service.RoomService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/map")
public class MapController {

    @Autowired
    private NaverMapService naverMapService;
    @Autowired
    private RoomService roomService;


    @GetMapping("/test")
    public String testConfig() {
        return naverMapService.testConfig();
    }

    @PostMapping("/route/{roomId}")
    public ResponseEntity<RouteResponse> calculateRouteFromRoom(@PathVariable Long roomId) throws JsonProcessingException {
        Room room = roomService.getRoomEntity(roomId);

        RouteResponse response = naverMapService.calculateRoute(
                room.getStartLatitude().doubleValue(),
                room.getStartLongitude().doubleValue(),
                room.getEndLatitude().doubleValue(),
                room.getEndLongitude().doubleValue()
        );

        room.setEstimatedDistance(response.getDistance());
        room.setEstimatedDuration(response.getDuration());
        room.setEstimatedTaxiFare(response.getTaxiFare());

        String pathJson = new ObjectMapper().writeValueAsString(response.getPath());
        room.setRoutePath(pathJson);

        roomService.saveRoom(room);

        return response.isSuccess()
                ? ResponseEntity.ok(response)
                : ResponseEntity.badRequest().body(response);
    }
}