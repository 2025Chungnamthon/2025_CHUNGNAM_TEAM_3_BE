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
    
    private static final String ROOM_CACHE_PREFIX = "room:";
    private static final String ROOM_PARTICIPANTS_PREFIX = "room:participants:";
    private static final String ROOM_EXCHANGE = "room.exchange";
    private static final String ROOM_CREATED_ROUTING_KEY = "room.created";
    private static final String ROOM_JOINED_ROUTING_KEY = "room.joined";
    
    public RoomService(RoomRepository roomRepository, UserRepository userRepository,
                       RoomParticipantRepository participantRepository,
                       RedisTemplate<String, Object> redisTemplate,
                       RabbitTemplate rabbitTemplate) {
        this.roomRepository = roomRepository;
        this.userRepository = userRepository;
        this.participantRepository = participantRepository;
        this.redisTemplate = redisTemplate;
        this.rabbitTemplate = rabbitTemplate;
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
    
    public RoomResponse joinRoom(JoinRoomRequest request) {
        Long roomId = request.getRoomId();
        Long userId = request.getUserId();

        Room room = getRoomFromCacheOrDb(roomId);
        if (room == null) throw new RuntimeException("방 없음");

        Optional<RoomParticipant> existingParticipant =
                participantRepository.findByRoomIdAndUserIdAndIsActiveTrue(roomId, userId);
        
        if (existingParticipant.isPresent()) {
            throw new RuntimeException("이미 해당 방에 참여 중입니다.");
        }
        
        // 방이 가득 찬지 확인
        if (room.getCurrentParticipants() >= room.getMaxParticipants()) {
            throw new RuntimeException("방이 가득 찼습니다.");
        }
        
        // 참여자 추가
        RoomParticipant participant = new RoomParticipant();
        participant.setRoomId(roomId);
        participant.setUserId(userId);
        participant.setRole(RoomParticipant.ParticipantRole.PASSENGER);
        participant.setIsActive(true);
        participant.setIsPaid(false);
        participantRepository.save(participant);
        
        // 방의 현재 참여자 수 증가
        room.setCurrentParticipants(room.getCurrentParticipants() + 1);
        roomRepository.save(room);
        
        try {
            // Redis 캐시 업데이트
            String cacheKey = ROOM_CACHE_PREFIX + roomId;
            redisTemplate.opsForValue().set(cacheKey, room, 1, TimeUnit.HOURS);
            
            // Redis에 참여자 정보 저장
            String participantsCacheKey = ROOM_PARTICIPANTS_PREFIX + roomId;
            redisTemplate.opsForSet().add(participantsCacheKey, userId);
            redisTemplate.expire(participantsCacheKey, 1, TimeUnit.HOURS);
        } catch (Exception e) {
            // Redis 오류가 있어도 계속 진행
            System.err.println("Redis cache error: " + e.getMessage());
        }
        
        try {
            // RabbitMQ로 방 참여 이벤트 발송
            rabbitTemplate.convertAndSend(ROOM_EXCHANGE, ROOM_JOINED_ROUTING_KEY, 
                new RoomJoinEvent(roomId, userId));
        } catch (Exception e) {
            // RabbitMQ 오류가 있어도 계속 진행
            System.err.println("RabbitMQ send error: " + e.getMessage());
        }
        
        return new RoomResponse(room);
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
}