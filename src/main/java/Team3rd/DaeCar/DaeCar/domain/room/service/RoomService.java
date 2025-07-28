package Team3rd.DaeCar.DaeCar.domain.room.service;

import Team3rd.DaeCar.DaeCar.domain.room.dto.CreateRoomRequest;
import Team3rd.DaeCar.DaeCar.domain.room.dto.JoinRoomRequest;
import Team3rd.DaeCar.DaeCar.domain.room.dto.RoomResponse;
import Team3rd.DaeCar.DaeCar.domain.room.dto.RoomParticipantResponse;
import Team3rd.DaeCar.DaeCar.domain.room.entity.Room;
import Team3rd.DaeCar.DaeCar.domain.room.entity.RoomParticipant;
import Team3rd.DaeCar.DaeCar.domain.room.enums.RoomType;
import Team3rd.DaeCar.DaeCar.domain.room.repository.RoomRepository;
import Team3rd.DaeCar.DaeCar.domain.room.repository.RoomParticipantRepository;
import Team3rd.DaeCar.DaeCar.domain.user.entity.User;
import Team3rd.DaeCar.DaeCar.domain.user.repository.UserRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import Team3rd.DaeCar.DaeCar.domain.map.service.NaverMapService;
import Team3rd.DaeCar.DaeCar.domain.map.dto.RouteResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Transactional
public class RoomService {
    
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final RoomParticipantRepository participantRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RabbitTemplate rabbitTemplate;

    private final NaverMapService naverMapService;
    private final ObjectMapper objectMapper;
    
    private static final String ROOM_CACHE_PREFIX = "room:";
    private static final String ROOM_PARTICIPANTS_PREFIX = "room:participants:";
    private static final String ROOM_EXCHANGE = "room.exchange";
    private static final String ROOM_CREATED_ROUTING_KEY = "room.created";
    private static final String ROOM_JOINED_ROUTING_KEY = "room.joined";
    
    public RoomService(RoomRepository roomRepository, UserRepository userRepository,
                       RoomParticipantRepository participantRepository,
                       RedisTemplate<String, Object> redisTemplate,
                       RabbitTemplate rabbitTemplate, NaverMapService naverMapService, ObjectMapper objectMapper) {
        this.roomRepository = roomRepository;
        this.userRepository = userRepository;
        this.participantRepository = participantRepository;
        this.redisTemplate = redisTemplate;
        this.rabbitTemplate = rabbitTemplate;
        this.naverMapService = naverMapService;
        this.objectMapper = objectMapper;
    }
    
    public RoomResponse createRoom(CreateRoomRequest request) {
        Room room = new Room();
        room.setName(request.getName());
        room.setMaxParticipants(request.getMaxParticipants());
        room.setCurrentParticipants(1); // 방 생성자가 첫 번째 참여자
        room.setRoomType(request.getRoomType());
        room.setDepartureLocation(request.getDepartureLocation());
        room.setDestination(request.getDestination());
        room.setDepartureTime(request.getDepartureTime());
        room.setCostPerPerson(request.getCostPerPerson());
        room.setDescription(request.getDescription());
        
        Room savedRoom = roomRepository.save(room);
        
        // 방 생성자를 CREATOR로 참여자에 추가
        RoomParticipant creator = new RoomParticipant();
        creator.setRoomId(savedRoom.getId());
        creator.setUserId(request.getUserId());
        creator.setRole(RoomParticipant.ParticipantRole.CREATOR);
        creator.setIsActive(true);
        creator.setIsPaid(false);
        participantRepository.save(creator);
        
        try {
            // Redis 캐시에 저장
            String cacheKey = ROOM_CACHE_PREFIX + savedRoom.getId();
            redisTemplate.opsForValue().set(cacheKey, savedRoom, 1, TimeUnit.HOURS);
        } catch (Exception e) {
            // Redis 오류가 있어도 계속 진행
            System.err.println("Redis cache error: " + e.getMessage());
        }
        
        try {
            // RabbitMQ로 방 생성 이벤트 발송
            rabbitTemplate.convertAndSend(ROOM_EXCHANGE, ROOM_CREATED_ROUTING_KEY, savedRoom);
        } catch (Exception e) {
            // RabbitMQ 오류가 있어도 계속 진행
            System.err.println("RabbitMQ send error: " + e.getMessage());
        }
        
        return new RoomResponse(savedRoom);
    }
    
    @Deprecated
    public RoomResponse joinRoom(JoinRoomRequest request) {
        // 이 메서드는 더 이상 직접 참여에 사용되지 않습니다.
        // 대신 RoomJoinRequestService를 통한 요청-승인 프로세스를 사용하세요.
        throw new RuntimeException("직접 참여는 더 이상 지원되지 않습니다. 참여 요청을 통해 승인받아야 합니다.");
    }
    
    public List<RoomResponse> getAvailableRooms() {
        return roomRepository.findAvailableRooms()
            .stream()
            .map(RoomResponse::new)
            .collect(Collectors.toList());
    }
    
    // 타입별 방 조회
    public List<RoomResponse> getAvailableRoomsByType(RoomType roomType) {
        return roomRepository.findAvailableRoomsByType(roomType)
            .stream()
            .map(RoomResponse::new)
            .collect(Collectors.toList());
    }
    
    // 카풀방만 조회
    public List<RoomResponse> getCarpoolRooms() {
        return getAvailableRoomsByType(RoomType.CARPOOL);
    }
    
    // 택시방만 조회
    public List<RoomResponse> getTaxiRooms() {
        return getAvailableRoomsByType(RoomType.TAXI);
    }
    
    
    public RoomResponse getRoomById(Long roomId) {
        Room room = getRoomFromCacheOrDb(roomId);
        if (room == null) {
            throw new RuntimeException("방을 찾을 수 없습니다.");
        }
        return new RoomResponse(room);
    }
    
    public List<RoomParticipantResponse> getRoomParticipants(Long roomId) {
        // 방이 존재하는지 확인
        Room room = getRoomFromCacheOrDb(roomId);
        if (room == null) {
            throw new RuntimeException("방을 찾을 수 없습니다.");
        }
        
        // 활성 상태인 참여자들만 조회
        List<RoomParticipant> participants = participantRepository.findByRoomIdAndIsActiveTrue(roomId);
        
        return participants.stream()
            .map(RoomParticipantResponse::new)
            .collect(Collectors.toList());
    }
    
    private Room getRoomFromCacheOrDb(Long roomId) {
        try {
            String cacheKey = ROOM_CACHE_PREFIX + roomId;
            Room cachedRoom = (Room) redisTemplate.opsForValue().get(cacheKey);
            
            if (cachedRoom != null) {
                return cachedRoom;
            }
        } catch (Exception e) {
            System.err.println("Redis cache error: " + e.getMessage());
        }
        
        Optional<Room> roomOpt = roomRepository.findByIdAndIsActiveTrue(roomId);
        if (roomOpt.isPresent()) {
            Room room = roomOpt.get();
            try {
                String cacheKey = ROOM_CACHE_PREFIX + roomId;
                redisTemplate.opsForValue().set(cacheKey, room, 1, TimeUnit.HOURS);
            } catch (Exception e) {
                System.err.println("Redis cache error: " + e.getMessage());
            }
            return room;
        }
        
        return null;
    }
    
    public void deleteRoom(Long roomId) {
        Room room = getRoomFromCacheOrDb(roomId);
        if (room == null) {
            throw new RuntimeException("방을 찾을 수 없습니다.");
        }
        
        // 방을 비활성화 (soft delete)
        room.setIsActive(false);
        roomRepository.save(room);
        
        // 해당 방의 모든 참여자를 비활성화
        List<RoomParticipant> participants = participantRepository.findByRoomIdAndIsActiveTrue(roomId);
        for (RoomParticipant participant : participants) {
            participant.setIsActive(false);
            participantRepository.save(participant);
        }
        
        try {
            // Redis 캐시에서 방 정보 삭제
            String cacheKey = ROOM_CACHE_PREFIX + roomId;
            redisTemplate.delete(cacheKey);
            
            // Redis에서 참여자 정보 삭제
            String participantsCacheKey = ROOM_PARTICIPANTS_PREFIX + roomId;
            redisTemplate.delete(participantsCacheKey);
        } catch (Exception e) {
            System.err.println("Redis cache delete error: " + e.getMessage());
        }
        
        try {
            // RabbitMQ로 방 삭제 이벤트 발송
            rabbitTemplate.convertAndSend(ROOM_EXCHANGE, "room.deleted", 
                new RoomDeleteEvent(roomId));
        } catch (Exception e) {
            System.err.println("RabbitMQ send error: " + e.getMessage());
        }
    }
    
    public void leaveRoom(Long roomId, Long userId) {
        Room room = getRoomFromCacheOrDb(roomId);
        if (room == null) {
            throw new RuntimeException("방을 찾을 수 없습니다.");
        }
        
        Optional<RoomParticipant> participantOpt = 
            participantRepository.findByRoomIdAndUserIdAndIsActiveTrue(roomId, userId);
        
        if (!participantOpt.isPresent()) {
            throw new RuntimeException("해당 방에 참여하지 않은 사용자입니다.");
        }
        
        RoomParticipant participant = participantOpt.get();
        
        // 방장이 나가는 경우 방을 삭제
        if (participant.isCreator()) {
            deleteRoom(roomId);
            return;
        }
        
        // 참여자를 비활성화
        participant.setIsActive(false);
        participantRepository.save(participant);
        
        // 방의 현재 참여자 수 감소
        room.setCurrentParticipants(room.getCurrentParticipants() - 1);
        roomRepository.save(room);
        
        try {
            // Redis 캐시 업데이트
            String cacheKey = ROOM_CACHE_PREFIX + roomId;
            redisTemplate.opsForValue().set(cacheKey, room, 1, TimeUnit.HOURS);
            
            // Redis에서 참여자 정보 삭제
            String participantsCacheKey = ROOM_PARTICIPANTS_PREFIX + roomId;
            redisTemplate.opsForSet().remove(participantsCacheKey, userId);
        } catch (Exception e) {
            System.err.println("Redis cache error: " + e.getMessage());
        }
        
        try {
            // RabbitMQ로 방 나가기 이벤트 발송
            rabbitTemplate.convertAndSend(ROOM_EXCHANGE, "room.left", 
                new RoomLeaveEvent(roomId, userId));
        } catch (Exception e) {
            System.err.println("RabbitMQ send error: " + e.getMessage());
        }
    }

    public Room getRoomEntity(Long roomId) {
        return roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("방을 찾을 수 없습니다."));
    }

    public static class RoomJoinEvent {
        private Long roomId;
        private Long userId;

        public RoomJoinEvent(Long roomId, Long userId) {
            this.roomId = roomId;
            this.userId = userId;
        }

        public Long getRoomId() { return roomId; }
        public Long getUserId() { return userId; }
    }
    
    public static class RoomDeleteEvent {
        private Long roomId;
        
        public RoomDeleteEvent(Long roomId) {
            this.roomId = roomId;
        }
        
        public Long getRoomId() {
            return roomId;
        }
    }
    
    public static class RoomLeaveEvent {
        private Long roomId;
        private Long userId;
        
        public RoomLeaveEvent(Long roomId, Long userId) {
            this.roomId = roomId;
            this.userId = userId;
        }
        
        public Long getRoomId() { return roomId; }
        public Long getUserId() { return userId; }
    }


    @Transactional
    public boolean setRoomCostFromNaverMap(Long roomId, double startLat, double startLng,
                                           double endLat, double endLng) {
        try {
            // 1. 네이버맵 API 호출
            RouteResponse routeResponse = naverMapService.calculateRoute(startLat, startLng, endLat, endLng);

            if (!routeResponse.isSuccess()) {
                return false;
            }

            // 2. 방 정보 가져오기
            Room room = roomRepository.findById(roomId)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카풀방입니다."));

            // 3. 🔥 택시요금 그대로 totalCost에 설정
            BigDecimal taxiCost = BigDecimal.valueOf(routeResponse.gettexiCost());
            room.setTotalCost(taxiCost);

            // 4. 기타 정보도 저장 (선택사항)
            room.setEstimatedDistance(routeResponse.getDistance());
            room.setEstimatedDuration(routeResponse.getDuration());
            room.setEstimatedTaxiFare(routeResponse.gettexiCost());

            roomRepository.save(room);

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public BigDecimal getCurrentPerPersonCost(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카풀방입니다."));

        if (room.getTotalCost() == null) {
            return BigDecimal.ZERO;
        }

        // 승객 수로 나누기 (운전자 제외)
        int passengerCount = room.getCurrentParticipants() - 1; // 운전자 1명 제외

        if (passengerCount <= 0) {
            return BigDecimal.ZERO;
        }

        return room.getTotalCost()
                .divide(BigDecimal.valueOf(passengerCount), java.math.RoundingMode.HALF_UP);
    }
}