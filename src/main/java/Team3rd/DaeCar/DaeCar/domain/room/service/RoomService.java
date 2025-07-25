package Team3rd.DaeCar.DaeCar.domain.room.service;

import Team3rd.DaeCar.DaeCar.domain.room.dto.CreateRoomRequest;
import Team3rd.DaeCar.DaeCar.domain.room.dto.JoinRoomRequest;
import Team3rd.DaeCar.DaeCar.domain.room.dto.RoomResponse;
import Team3rd.DaeCar.DaeCar.domain.room.dto.RoomParticipantResponse;
import Team3rd.DaeCar.DaeCar.domain.room.entity.Room;
import Team3rd.DaeCar.DaeCar.domain.room.entity.RoomParticipant;
import Team3rd.DaeCar.DaeCar.domain.room.repository.RoomRepository;
import Team3rd.DaeCar.DaeCar.domain.room.repository.RoomParticipantRepository;
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
    private final RoomParticipantRepository participantRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RabbitTemplate rabbitTemplate;
    
    private static final String ROOM_CACHE_PREFIX = "room:";
    private static final String ROOM_PARTICIPANTS_PREFIX = "room:participants:";
    private static final String ROOM_EXCHANGE = "room.exchange";
    private static final String ROOM_CREATED_ROUTING_KEY = "room.created";
    private static final String ROOM_JOINED_ROUTING_KEY = "room.joined";
    
    public RoomService(RoomRepository roomRepository, 
                      RoomParticipantRepository participantRepository,
                      RedisTemplate<String, Object> redisTemplate,
                      RabbitTemplate rabbitTemplate) {
        this.roomRepository = roomRepository;
        this.participantRepository = participantRepository;
        this.redisTemplate = redisTemplate;
        this.rabbitTemplate = rabbitTemplate;
    }
    
    public RoomResponse createRoom(CreateRoomRequest request) {
        Room room = new Room();
        room.setName(request.getName());
        room.setMaxParticipants(request.getMaxParticipants());
        room.setCurrentParticipants(0);
        
        Room savedRoom = roomRepository.save(room);
        
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
        String userId = request.getUserId();
        
        // 이미 참여 중인지 확인
        Optional<RoomParticipant> existingParticipant = 
            participantRepository.findByRoomIdAndUserIdAndIsActiveTrue(roomId, userId);
        
        if (existingParticipant.isPresent()) {
            throw new RuntimeException("이미 해당 방에 참여 중입니다.");
        }
        
        // 방 정보 조회
        Room room = getRoomFromCacheOrDb(roomId);
        if (room == null) {
            throw new RuntimeException("방을 찾을 수 없습니다.");
        }
        
        // 방이 가득 찬지 확인
        if (room.getCurrentParticipants() >= room.getMaxParticipants()) {
            throw new RuntimeException("방이 가득 찼습니다.");
        }
        
        // 참여자 추가
        RoomParticipant participant = new RoomParticipant();
        participant.setRoomId(roomId);
        participant.setUserId(userId);
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
    
    public static class RoomJoinEvent {
        private Long roomId;
        private String userId;
        
        public RoomJoinEvent(Long roomId, String userId) {
            this.roomId = roomId;
            this.userId = userId;
        }
        
        public Long getRoomId() {
            return roomId;
        }
        
        public String getUserId() {
            return userId;
        }
    }
}