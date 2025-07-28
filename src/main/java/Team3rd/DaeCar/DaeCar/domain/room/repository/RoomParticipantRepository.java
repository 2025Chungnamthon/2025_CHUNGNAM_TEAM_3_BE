package Team3rd.DaeCar.DaeCar.domain.room.repository;

import Team3rd.DaeCar.DaeCar.domain.room.entity.Room;
import Team3rd.DaeCar.DaeCar.domain.room.entity.RoomParticipant;
import Team3rd.DaeCar.DaeCar.domain.user.entity.User;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomParticipantRepository extends JpaRepository<RoomParticipant, Long> {

    List<RoomParticipant> findByRoomIdAndIsActiveTrue(Long roomId);

    List<RoomParticipant> findByUserIdAndIsActiveTrue(Long userId);

    Optional<RoomParticipant> findByRoomIdAndUserIdAndIsActiveTrue(Long roomId, Long userId); // ✅ 이거 꼭 필요

    int countByRoomIdAndIsActiveTrue(Long roomId);

}