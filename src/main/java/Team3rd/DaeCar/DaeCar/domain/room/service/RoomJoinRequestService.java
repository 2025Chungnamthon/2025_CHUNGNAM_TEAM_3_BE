package Team3rd.DaeCar.DaeCar.domain.room.service;

import Team3rd.DaeCar.DaeCar.domain.room.dto.RoomJoinRequestDto;
import Team3rd.DaeCar.DaeCar.domain.room.entity.Room;
import Team3rd.DaeCar.DaeCar.domain.room.entity.RoomJoinRequest;
import Team3rd.DaeCar.DaeCar.domain.room.entity.RoomParticipant;
import Team3rd.DaeCar.DaeCar.domain.room.repository.RoomJoinRequestRepository;
import Team3rd.DaeCar.DaeCar.domain.room.repository.RoomParticipantRepository;
import Team3rd.DaeCar.DaeCar.domain.room.repository.RoomRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Transactional
public class RoomJoinRequestService {
    
    private final RoomJoinRequestRepository joinRequestRepository;
    private final RoomRepository roomRepository;
    private final RoomParticipantRepository participantRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RabbitTemplate rabbitTemplate;
    
    private static final String ROOM_EXCHANGE = "room.exchange";
    private static final String JOIN_REQUEST_ROUTING_KEY = "room.join.request";
    private static final String JOIN_REQUEST_PROCESSED_ROUTING_KEY = "room.join.processed";
    
    public RoomJoinRequestService(RoomJoinRequestRepository joinRequestRepository,
                                 RoomRepository roomRepository,
                                 RoomParticipantRepository participantRepository,
                                 RedisTemplate<String, Object> redisTemplate,
                                 RabbitTemplate rabbitTemplate) {
        this.joinRequestRepository = joinRequestRepository;
        this.roomRepository = roomRepository;
        this.participantRepository = participantRepository;
        this.redisTemplate = redisTemplate;
        this.rabbitTemplate = rabbitTemplate;
    }
    
    public RoomJoinRequestDto.Response createJoinRequest(RoomJoinRequestDto.CreateRequest request) {
        // 방이 존재하는지 확인
        Room room = roomRepository.findByIdAndIsActiveTrue(request.getRoomId())
                .orElseThrow(() -> new RuntimeException("존재하지 않는 방입니다."));
        
        // 이미 참여 중인지 확인
        Optional<RoomParticipant> existingParticipant = 
            participantRepository.findByRoomIdAndUserIdAndIsActiveTrue(request.getRoomId(), request.getUserId());
        if (existingParticipant.isPresent()) {
            throw new RuntimeException("이미 해당 방에 참여 중입니다.");
        }
        
        // 이미 대기 중인 요청이 있는지 확인
        boolean hasExistingRequest = joinRequestRepository.existsByRoomIdAndUserIdAndStatus(
            request.getRoomId(), request.getUserId(), RoomJoinRequest.RequestStatus.PENDING);
        if (hasExistingRequest) {
            throw new RuntimeException("이미 참여 요청을 보낸 방입니다.");
        }
        
        // 방이 가득 찬지 확인
        if (room.getCurrentParticipants() >= room.getMaxParticipants()) {
            throw new RuntimeException("방이 가득 찼습니다.");
        }
        
        // 참여 요청 생성
        RoomJoinRequest joinRequest = new RoomJoinRequest();
        joinRequest.setRoomId(request.getRoomId());
        joinRequest.setUserId(request.getUserId());
        joinRequest.setMessage(request.getMessage());
        joinRequest.setStatus(RoomJoinRequest.RequestStatus.PENDING);
        
        RoomJoinRequest savedRequest = joinRequestRepository.save(joinRequest);
        
        try {
            // RabbitMQ로 참여 요청 이벤트 발송
            rabbitTemplate.convertAndSend(ROOM_EXCHANGE, JOIN_REQUEST_ROUTING_KEY, 
                new JoinRequestEvent(savedRequest.getId(), savedRequest.getRoomId(), savedRequest.getUserId()));
        } catch (Exception e) {
            System.err.println("RabbitMQ send error: " + e.getMessage());
        }
        
        return new RoomJoinRequestDto.Response(savedRequest);
    }
    
    public RoomJoinRequestDto.Response processJoinRequest(RoomJoinRequestDto.ProcessRequest request) {
        // 참여 요청 조회
        RoomJoinRequest joinRequest = joinRequestRepository.findById(request.getRequestId())
                .orElseThrow(() -> new RuntimeException("존재하지 않는 참여 요청입니다."));
        
        if (!joinRequest.isPending()) {
            throw new RuntimeException("이미 처리된 참여 요청입니다.");
        }
        
        // 방 존재 확인
        Room room = roomRepository.findByIdAndIsActiveTrue(joinRequest.getRoomId())
                .orElseThrow(() -> new RuntimeException("존재하지 않는 방입니다."));
        
        // 처리자가 방장인지 확인
        Optional<RoomParticipant> creatorParticipant = 
            participantRepository.findByRoomIdAndUserIdAndIsActiveTrue(joinRequest.getRoomId(), request.getProcessedBy());
        if (!creatorParticipant.isPresent() || !creatorParticipant.get().isCreator()) {
            throw new RuntimeException("방장만 참여 요청을 처리할 수 있습니다.");
        }
        
        if (request.getApproved()) {
            // 승인 처리
            // 방이 가득 찬지 다시 확인
            if (room.getCurrentParticipants() >= room.getMaxParticipants()) {
                throw new RuntimeException("방이 가득 찼습니다.");
            }
            
            // 참여 요청 승인
            joinRequest.approve(request.getProcessedBy());
            
            // 실제 참여자로 추가
            RoomParticipant participant = new RoomParticipant();
            participant.setRoomId(joinRequest.getRoomId());
            participant.setUserId(joinRequest.getUserId());
            participant.setRole(RoomParticipant.ParticipantRole.PASSENGER);
            participant.setIsActive(true);
            participant.setIsPaid(false);
            participantRepository.save(participant);
            
            // 방 참여자 수 증가
            room.setCurrentParticipants(room.getCurrentParticipants() + 1);
            roomRepository.save(room);
            
        } else {
            // 거절 처리
            joinRequest.reject(request.getProcessedBy(), request.getRejectionReason());
        }
        
        RoomJoinRequest processedRequest = joinRequestRepository.save(joinRequest);
        
        try {
            // RabbitMQ로 처리 완료 이벤트 발송
            rabbitTemplate.convertAndSend(ROOM_EXCHANGE, JOIN_REQUEST_PROCESSED_ROUTING_KEY, 
                new JoinRequestProcessedEvent(processedRequest.getId(), processedRequest.getRoomId(), 
                    processedRequest.getUserId(), processedRequest.getStatus(), request.getApproved()));
        } catch (Exception e) {
            System.err.println("RabbitMQ send error: " + e.getMessage());
        }
        
        return new RoomJoinRequestDto.Response(processedRequest);
    }
    
    @Transactional(readOnly = true)
    public List<RoomJoinRequestDto.Response> getRoomJoinRequests(Long roomId) {
        Room room = roomRepository.findByIdAndIsActiveTrue(roomId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 방입니다."));
        
        List<RoomJoinRequest> requests = joinRequestRepository.findByRoomIdOrderByRequestedAtDesc(roomId);
        return requests.stream()
                .map(RoomJoinRequestDto.Response::new)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<RoomJoinRequestDto.Response> getPendingJoinRequests(Long roomId) {
        Room room = roomRepository.findByIdAndIsActiveTrue(roomId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 방입니다."));
        
        List<RoomJoinRequest> requests = joinRequestRepository.findByRoomIdAndStatusOrderByRequestedAtDesc(
            roomId, RoomJoinRequest.RequestStatus.PENDING);
        return requests.stream()
                .map(RoomJoinRequestDto.Response::new)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<RoomJoinRequestDto.Response> getUserJoinRequests(Long userId) {
        List<RoomJoinRequest> requests = joinRequestRepository.findByUserIdOrderByRequestedAtDesc(userId);
        return requests.stream()
                .map(RoomJoinRequestDto.Response::new)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public RoomJoinRequestDto.Response getJoinRequestById(Long requestId) {
        RoomJoinRequest request = joinRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 참여 요청입니다."));
        return new RoomJoinRequestDto.Response(request);
    }
    
    public void cancelJoinRequest(Long requestId, Long userId) {
        RoomJoinRequest request = joinRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 참여 요청입니다."));
        
        if (!request.getUserId().equals(userId)) {
            throw new RuntimeException("본인의 요청만 취소할 수 있습니다.");
        }
        
        if (!request.isPending()) {
            throw new RuntimeException("대기 중인 요청만 취소할 수 있습니다.");
        }
        
        joinRequestRepository.delete(request);
    }
    
    // 이벤트 클래스들
    public static class JoinRequestEvent {
        private Long requestId;
        private Long roomId;
        private Long userId;
        
        public JoinRequestEvent(Long requestId, Long roomId, Long userId) {
            this.requestId = requestId;
            this.roomId = roomId;
            this.userId = userId;
        }
        
        public Long getRequestId() { return requestId; }
        public Long getRoomId() { return roomId; }
        public Long getUserId() { return userId; }
    }
    
    public static class JoinRequestProcessedEvent {
        private Long requestId;
        private Long roomId;
        private Long userId;
        private RoomJoinRequest.RequestStatus status;
        private Boolean approved;
        
        public JoinRequestProcessedEvent(Long requestId, Long roomId, Long userId, 
                                       RoomJoinRequest.RequestStatus status, Boolean approved) {
            this.requestId = requestId;
            this.roomId = roomId;
            this.userId = userId;
            this.status = status;
            this.approved = approved;
        }
        
        public Long getRequestId() { return requestId; }
        public Long getRoomId() { return roomId; }
        public Long getUserId() { return userId; }
        public RoomJoinRequest.RequestStatus getStatus() { return status; }
        public Boolean getApproved() { return approved; }
    }
}