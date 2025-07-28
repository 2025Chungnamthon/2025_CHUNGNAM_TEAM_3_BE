package Team3rd.DaeCar.DaeCar.domain.pay.repository;

import Team3rd.DaeCar.DaeCar.domain.pay.entity.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {

    // 특정 카풀방의 모든 결제 내역 조회
    List<PaymentTransaction> findByRoomIdOrderByCreatedAtDesc(Long roomId);

    // 특정 사용자가 받은 결제 내역 (운전자 수익 조회)
    List<PaymentTransaction> findByToUserIdOrderByCreatedAtDesc(Long toUserId);

    // 특정 사용자가 한 결제 내역 (승객 지출 조회)
    List<PaymentTransaction> findByFromUserIdOrderByCreatedAtDesc(Long fromUserId);

    // 성공한 결제만 조회
    List<PaymentTransaction> findByStatusOrderByCreatedAtDesc(PaymentTransaction.TransactionStatus status);

    // 특정 기간 내 결제 내역
    @Query("SELECT pt FROM PaymentTransaction pt WHERE pt.createdAt BETWEEN :startDate AND :endDate ORDER BY pt.createdAt DESC")
    List<PaymentTransaction> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate,
                                                    @Param("endDate") LocalDateTime endDate);

    // 특정 방의 성공한 결제 총액 계산
    @Query("SELECT COALESCE(SUM(pt.amount), 0) FROM PaymentTransaction pt WHERE pt.roomId = :roomId AND pt.status = 'SUCCESS'")
    Long getTotalSuccessfulPaymentsByRoom(@Param("roomId") Long roomId);
}