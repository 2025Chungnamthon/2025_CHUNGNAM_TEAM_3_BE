package Team3rd.DaeCar.DaeCar.domain.map.controller;

import Team3rd.DaeCar.DaeCar.domain.map.dto.RouteRequest;
import Team3rd.DaeCar.DaeCar.domain.map.dto.RouteResponse;
import Team3rd.DaeCar.DaeCar.domain.map.service.NaverMapService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/map")
public class MapController {

    @Autowired
    private NaverMapService naverMapService;

    @GetMapping("/test")
    public String testConfig() {
        return naverMapService.testConfig();
    }

    @PostMapping("/route")
    public ResponseEntity<RouteResponse> calculateRoute(@Valid @RequestBody RouteRequest request) {
        RouteResponse response = naverMapService.calculateRoute(
                request.getStartLatitude(),
                request.getStartLongitude(),
                request.getEndLatitude(),
                request.getEndLongitude()
        );

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
}