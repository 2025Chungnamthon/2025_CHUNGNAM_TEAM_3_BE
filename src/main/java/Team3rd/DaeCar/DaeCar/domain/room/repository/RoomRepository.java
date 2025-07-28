package Team3rd.DaeCar.DaeCar.domain.room.repository;

import Team3rd.DaeCar.DaeCar.domain.room.entity.Room;
import Team3rd.DaeCar.DaeCar.domain.room.enums.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    List<Room> findByIsActiveTrue();
    
    Optional<Room> findByIdAndIsActiveTrue(Long id);
    
    @Query("SELECT r FROM Room r WHERE r.isActive = true AND r.currentParticipants < r.maxParticipants")
    List<Room> findAvailableRooms();
    
    // 타입별 방 조회
    List<Room> findByRoomTypeAndIsActiveTrue(RoomType roomType);
    
    @Query("SELECT r FROM Room r WHERE r.roomType = :roomType AND r.isActive = true AND r.currentParticipants < r.maxParticipants")
    List<Room> findAvailableRoomsByType(RoomType roomType);
    
}