package Team3rd.DaeCar.DaeCar.domain.room.controller;

import Team3rd.DaeCar.DaeCar.domain.pay.service.CarpoolPaymentService;
import Team3rd.DaeCar.DaeCar.domain.room.dto.CreateRoomRequest;
import Team3rd.DaeCar.DaeCar.domain.room.dto.JoinRoomRequest;
import Team3rd.DaeCar.DaeCar.domain.room.dto.RoomResponse;
import Team3rd.DaeCar.DaeCar.domain.room.dto.RoomParticipantResponse;
import Team3rd.DaeCar.DaeCar.domain.room.entity.Room;
import Team3rd.DaeCar.DaeCar.domain.room.enums.RoomType;
import Team3rd.DaeCar.DaeCar.domain.room.service.RoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/rooms")
@Tag(name = "Room", description = "방 관리 API")
public class RoomController {
    
    private final RoomService roomService;
    
    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }
    
    @PostMapping
    @Operation(summary = "방 생성", description = "새로운 방을 생성합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "방 생성 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    public ResponseEntity<RoomResponse> createRoom(@Valid @RequestBody CreateRoomRequest request) {
        try {
            RoomResponse response = roomService.createRoom(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    
    @PostMapping("/join")
    @Operation(summary = "방 참가", description = "기존 방에 참가합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "방 참가 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "500", description = "내부 서버 오류")
    })
    public ResponseEntity<RoomResponse> joinRoom(@Valid @RequestBody JoinRoomRequest request) {
        try {
            RoomResponse response = roomService.joinRoom(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/available")
    @Operation(summary = "사용 가능한 방 목록", description = "현재 참가 가능한 방 목록을 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "500", description = "내부 서버 오류")
    })
    public ResponseEntity<List<RoomResponse>> getAvailableRooms() {
        try {
            List<RoomResponse> rooms = roomService.getAvailableRooms();
            return ResponseEntity.ok(rooms);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/carpool")
    @Operation(summary = "카풀방 목록", description = "참가 가능한 카풀방 목록을 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "500", description = "내부 서버 오류")
    })
    public ResponseEntity<List<RoomResponse>> getCarpoolRooms() {
        try {
            List<RoomResponse> rooms = roomService.getCarpoolRooms();
            return ResponseEntity.ok(rooms);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/taxi")
    @Operation(summary = "택시방 목록", description = "참가 가능한 택시방 목록을 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "500", description = "내부 서버 오류")
    })
    public ResponseEntity<List<RoomResponse>> getTaxiRooms() {
        try {
            List<RoomResponse> rooms = roomService.getTaxiRooms();
            return ResponseEntity.ok(rooms);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    
    @GetMapping("/{roomId}")
    @Operation(summary = "방 상세 조회", description = "특정 방의 상세 정보를 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "404", description = "방을 찾을 수 없음"),
        @ApiResponse(responseCode = "500", description = "내부 서버 오류")
    })
    public ResponseEntity<RoomResponse> getRoomById(
            @Parameter(description = "방 ID", required = true) @PathVariable Long roomId) {
        try {
            RoomResponse response = roomService.getRoomById(roomId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{roomId}/participants")
    @Operation(summary = "방 참여자 목록 조회", description = "특정 방의 참여자 목록을 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "404", description = "방을 찾을 수 없음"),
        @ApiResponse(responseCode = "500", description = "내부 서버 오류")
    })
    public ResponseEntity<List<RoomParticipantResponse>> getRoomParticipants(
            @Parameter(description = "방 ID", required = true) @PathVariable Long roomId) {
        try {
            List<RoomParticipantResponse> participants = roomService.getRoomParticipants(roomId);
            return ResponseEntity.ok(participants);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{roomId}")
    @Operation(summary = "방 삭제", description = "특정 방을 삭제합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "삭제 성공"),
        @ApiResponse(responseCode = "404", description = "방을 찾을 수 없음"),
        @ApiResponse(responseCode = "500", description = "내부 서버 오류")
    })
    public ResponseEntity<Void> deleteRoom(
            @Parameter(description = "방 ID", required = true) @PathVariable Long roomId) {
        try {
            roomService.deleteRoom(roomId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/{roomId}/leave")
    @Operation(summary = "방 나가기", description = "현재 참여 중인 방에서 나갑니다. 방장이 나가면 방이 삭제됩니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "방 나가기 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "404", description = "방을 찾을 수 없음"),
        @ApiResponse(responseCode = "500", description = "내부 서버 오류")
    })
    public ResponseEntity<Void> leaveRoom(
            @Parameter(description = "방 ID", required = true) @PathVariable Long roomId,
            @Parameter(description = "사용자 ID", required = true) @RequestParam Long userId) {
        try {
            roomService.leaveRoom(roomId, userId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    @GetMapping("/{roomId}/preview")
    @Operation(summary = "결제 미리보기", description = "특정 방의 예상 결제 금액, 거리, 시간 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "미리보기 조회 성공"),
            @ApiResponse(responseCode = "404", description = "방을 찾을 수 없음"),
            @ApiResponse(responseCode = "500", description = "내부 서버 오류")
    })
    public ResponseEntity<CarpoolPaymentService.PaymentPreview> previewPayment(
            @Parameter(description = "방 ID", required = true) @PathVariable Long roomId) {
        try {
            Room room = roomService.getRoomEntity(roomId);

            BigDecimal totalCost = room.getTotalCost() != null ? room.getTotalCost() : BigDecimal.ZERO;
            BigDecimal perPersonCost = room.getCostPerPerson() != null ? room.getCostPerPerson() : BigDecimal.ZERO;
            int passengerCount = room.getCurrentParticipants() != null ? room.getCurrentParticipants() : 0;
            String distance = room.getEstimatedDistance() != null ? room.getEstimatedDistance() + "m" : "정보 없음";
            String duration = room.getEstimatedDuration() != null ? (room.getEstimatedDuration() / 60000) + "분" : "정보 없음";
            String desc = String.format("🚗 %s → %s 예상 요금 (%,d원)", room.getDepartureLocation(), room.getDestination(), perPersonCost.intValue());

            CarpoolPaymentService.PaymentPreview preview = new CarpoolPaymentService.PaymentPreview(totalCost, perPersonCost, passengerCount, distance, duration, desc);
            return ResponseEntity.ok(preview);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


}