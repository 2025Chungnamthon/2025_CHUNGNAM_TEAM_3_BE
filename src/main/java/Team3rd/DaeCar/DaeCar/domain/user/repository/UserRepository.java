package Team3rd.DaeCar.DaeCar.domain.user.repository;

import Team3rd.DaeCar.DaeCar.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByEmailAndIsActiveTrue(String email);
    
    Optional<User> findByIdAndIsActiveTrue(Long id);
    
    boolean existsByEmailAndIsActiveTrue(String email);
    
    boolean existsByNicknameAndIsActiveTrue(String nickname);
}