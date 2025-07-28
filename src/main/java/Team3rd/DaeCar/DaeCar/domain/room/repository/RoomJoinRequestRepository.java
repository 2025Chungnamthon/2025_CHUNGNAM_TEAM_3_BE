package Team3rd.DaeCar.DaeCar.domain.room.repository;

import Team3rd.DaeCar.DaeCar.domain.room.entity.RoomJoinRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomJoinRequestRepository extends JpaRepository<RoomJoinRequest, Long> {
    
    // 특정 방의 모든 참여 요청 조회
    List<RoomJoinRequest> findByRoomIdOrderByRequestedAtDesc(Long roomId);
    
    // 특정 방의 특정 상태 참여 요청 조회
    List<RoomJoinRequest> findByRoomIdAndStatusOrderByRequestedAtDesc(Long roomId, RoomJoinRequest.RequestStatus status);
    
    // 특정 사용자의 모든 참여 요청 조회
    List<RoomJoinRequest> findByUserIdOrderByRequestedAtDesc(Long userId);
    
    // 특정 사용자의 특정 상태 참여 요청 조회
    List<RoomJoinRequest> findByUserIdAndStatusOrderByRequestedAtDesc(Long userId, RoomJoinRequest.RequestStatus status);
    
    // 특정 방에 대한 특정 사용자의 참여 요청 조회
    Optional<RoomJoinRequest> findByRoomIdAndUserId(Long roomId, Long userId);
    
    // 특정 방에 대한 특정 사용자의 대기 중인 참여 요청 조회
    Optional<RoomJoinRequest> findByRoomIdAndUserIdAndStatus(Long roomId, Long userId, RoomJoinRequest.RequestStatus status);
    
    // 특정 방의 대기 중인 참여 요청 개수
    @Query("SELECT COUNT(r) FROM RoomJoinRequest r WHERE r.roomId = :roomId AND r.status = 'PENDING'")
    long countPendingRequestsByRoomId(@Param("roomId") Long roomId);
    
    // 특정 사용자가 요청한 대기 중인 참여 요청이 있는지 확인
    boolean existsByRoomIdAndUserIdAndStatus(Long roomId, Long userId, RoomJoinRequest.RequestStatus status);
    
    // 특정 방의 승인된 참여 요청 개수
    @Query("SELECT COUNT(r) FROM RoomJoinRequest r WHERE r.roomId = :roomId AND r.status = 'APPROVED'")
    long countApprovedRequestsByRoomId(@Param("roomId") Long roomId);
}