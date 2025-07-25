package Team3rd.DaeCar.DaeCar.domain.room.repository;

import Team3rd.DaeCar.DaeCar.domain.room.entity.RoomParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomParticipantRepository extends JpaRepository<RoomParticipant, Long> {
    List<RoomParticipant> findByRoomIdAndIsActiveTrue(Long roomId);
    
    Optional<RoomParticipant> findByRoomIdAndUserIdAndIsActiveTrue(Long roomId, String userId);
    
    List<RoomParticipant> findByUserIdAndIsActiveTrue(String userId);
    
    int countByRoomIdAndIsActiveTrue(Long roomId);
}