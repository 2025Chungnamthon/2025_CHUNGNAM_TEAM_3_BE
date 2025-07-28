package Team3rd.DaeCar.DaeCar.domain.room.controller;

import Team3rd.DaeCar.DaeCar.domain.room.dto.RoomJoinRequestDto;
import Team3rd.DaeCar.DaeCar.domain.room.service.RoomJoinRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/room-join-requests")
@Tag(name = "RoomJoinRequest", description = "방 참여 요청 관리 API")
public class RoomJoinRequestController {
    
    private final RoomJoinRequestService joinRequestService;
    
    public RoomJoinRequestController(RoomJoinRequestService joinRequestService) {
        this.joinRequestService = joinRequestService;
    }
    
    @PostMapping
    @Operation(summary = "방 참여 요청", description = "특정 방에 참여 요청을 보냅니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "참여 요청 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "409", description = "이미 참여 중이거나 요청 중인 방")
    })
    public ResponseEntity<RoomJoinRequestDto.Response> createJoinRequest(
            @Valid @RequestBody RoomJoinRequestDto.CreateRequest request) {
        try {
            RoomJoinRequestDto.Response response = joinRequestService.createJoinRequest(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    
    @PostMapping("/process")
    @Operation(summary = "참여 요청 처리", description = "방장이 참여 요청을 승인 또는 거절합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "요청 처리 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "403", description = "권한 없음 (방장이 아님)"),
        @ApiResponse(responseCode = "404", description = "요청을 찾을 수 없음")
    })
    public ResponseEntity<RoomJoinRequestDto.Response> processJoinRequest(
            @Valid @RequestBody RoomJoinRequestDto.ProcessRequest request) {
        try {
            RoomJoinRequestDto.Response response = joinRequestService.processJoinRequest(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            if (e.getMessage().contains("방장만") || e.getMessage().contains("권한")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            } else if (e.getMessage().contains("존재하지 않는")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/room/{roomId}")
    @Operation(summary = "방의 모든 참여 요청 조회", description = "특정 방의 모든 참여 요청을 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "404", description = "방을 찾을 수 없음"),
        @ApiResponse(responseCode = "500", description = "내부 서버 오류")
    })
    public ResponseEntity<List<RoomJoinRequestDto.Response>> getRoomJoinRequests(
            @Parameter(description = "방 ID", required = true) @PathVariable Long roomId) {
        try {
            List<RoomJoinRequestDto.Response> requests = joinRequestService.getRoomJoinRequests(roomId);
            return ResponseEntity.ok(requests);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/room/{roomId}/pending")
    @Operation(summary = "방의 대기 중인 참여 요청 조회", description = "특정 방의 대기 중인 참여 요청만 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "404", description = "방을 찾을 수 없음"),
        @ApiResponse(responseCode = "500", description = "내부 서버 오류")
    })
    public ResponseEntity<List<RoomJoinRequestDto.Response>> getPendingJoinRequests(
            @Parameter(description = "방 ID", required = true) @PathVariable Long roomId) {
        try {
            List<RoomJoinRequestDto.Response> requests = joinRequestService.getPendingJoinRequests(roomId);
            return ResponseEntity.ok(requests);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/user/{userId}")
    @Operation(summary = "사용자의 참여 요청 조회", description = "특정 사용자가 보낸 모든 참여 요청을 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "500", description = "내부 서버 오류")
    })
    public ResponseEntity<List<RoomJoinRequestDto.Response>> getUserJoinRequests(
            @Parameter(description = "사용자 ID", required = true) @PathVariable Long userId) {
        try {
            List<RoomJoinRequestDto.Response> requests = joinRequestService.getUserJoinRequests(userId);
            return ResponseEntity.ok(requests);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/{requestId}")
    @Operation(summary = "참여 요청 상세 조회", description = "특정 참여 요청의 상세 정보를 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "404", description = "요청을 찾을 수 없음"),
        @ApiResponse(responseCode = "500", description = "내부 서버 오류")
    })
    public ResponseEntity<RoomJoinRequestDto.Response> getJoinRequestById(
            @Parameter(description = "요청 ID", required = true) @PathVariable Long requestId) {
        try {
            RoomJoinRequestDto.Response response = joinRequestService.getJoinRequestById(requestId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @DeleteMapping("/{requestId}")
    @Operation(summary = "참여 요청 취소", description = "대기 중인 참여 요청을 취소합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "취소 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "403", description = "권한 없음"),
        @ApiResponse(responseCode = "404", description = "요청을 찾을 수 없음")
    })
    public ResponseEntity<Void> cancelJoinRequest(
            @Parameter(description = "요청 ID", required = true) @PathVariable Long requestId,
            @Parameter(description = "사용자 ID", required = true) @RequestParam Long userId) {
        try {
            joinRequestService.cancelJoinRequest(requestId, userId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            if (e.getMessage().contains("본인의 요청만")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            } else if (e.getMessage().contains("존재하지 않는")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}