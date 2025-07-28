package Team3rd.DaeCar.DaeCar.domain.pay.service;

import Team3rd.DaeCar.DaeCar.domain.pay.entity.PaymentTransaction;
import Team3rd.DaeCar.DaeCar.domain.pay.repository.PaymentTransactionRepository;
import Team3rd.DaeCar.DaeCar.domain.room.entity.Room;
import Team3rd.DaeCar.DaeCar.domain.room.entity.RoomParticipant;
import Team3rd.DaeCar.DaeCar.domain.room.repository.RoomParticipantRepository;
import Team3rd.DaeCar.DaeCar.domain.room.repository.RoomRepository;
import Team3rd.DaeCar.DaeCar.domain.user.entity.User;
import Team3rd.DaeCar.DaeCar.domain.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CarpoolPaymentService {

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private RoomParticipantRepository roomParticipantRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PaymentTransactionRepository paymentTransactionRepository;

    /**
     * 🚗 카풀 완료 시 자동 결제 처리 (운전자가 도착 버튼 클릭)
     * @param roomId 카풀방 ID
     * @param driverUserId 운전자 ID
     * @return 결제 결과
     */
    @Transactional
    public PaymentResult completeTrip(Long roomId, Long driverUserId) {
        try {
            // 1. 방 정보 조회 및 검증
            Room room = roomRepository.findById(roomId)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카풀방입니다."));

            if (room.getStatus() != Room.RoomStatus.DRIVING) {
                return PaymentResult.failure("운행중인 카풀방이 아닙니다.");
            }

            if (!driverUserId.equals(room.getDriverUserId())) {
                return PaymentResult.failure("운전자만 도착 처리할 수 있습니다.");
            }

            // 2. 참여자들 조회
            List<RoomParticipant> participants = roomParticipantRepository
                    .findByRoomIdAndIsActiveTrue(roomId);

            // 3. 결제해야 할 승객들 찾기 (운전자 제외)
            List<RoomParticipant> passengers = participants.stream()
                    .filter(p -> !p.isDriver() && !p.getIsPaid())
                    .collect(Collectors.toList());

            if (passengers.isEmpty()) {
                room.setStatus(Room.RoomStatus.COMPLETED);
                roomRepository.save(room);
                return PaymentResult.success("이미 모든 결제가 완료되었습니다.");
            }

            // 4. 1인당 결제 금액 계산
            BigDecimal perPersonCost = room.getTotalCost()
                    .divide(BigDecimal.valueOf(passengers.size()), RoundingMode.HALF_UP);

            // 5. 각 승객이 운전자에게 결제
            int successCount = 0;
            for (RoomParticipant passenger : passengers) {
                Long passengerUserId = passenger.getUserId();
                boolean paymentSuccess = transferPoints(
                        passengerUserId,
                        driverUserId,
                        perPersonCost,
                        roomId,
                        generatePaymentDescription(room)
                );

                if (paymentSuccess) {
                    passenger.setIsPaid(true);
                    roomParticipantRepository.save(passenger);
                    successCount++;
                }
            }

            // 6. 방 상태를 완료로 변경
            room.setStatus(Room.RoomStatus.COMPLETED);
            roomRepository.save(room);

            return PaymentResult.success(
                    String.format("결제 완료! %d명 중 %d명 결제 성공",
                            passengers.size(), successCount)
            );

        } catch (Exception e) {
            return PaymentResult.failure("결제 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 💸 포인트 이체 처리
     */
    private boolean transferPoints(Long fromUserId, Long toUserId, BigDecimal amount,
                                   Long roomId, String description) {
        try {
            // 사용자들 조회
            User fromUser = userRepository.findById(fromUserId)
                    .orElseThrow(() -> new IllegalArgumentException("결제자를 찾을 수 없습니다."));
            User toUser = userRepository.findById(toUserId)
                    .orElseThrow(() -> new IllegalArgumentException("수취인을 찾을 수 없습니다."));

            // 포인트 부족 체크
            if (fromUser.getPoints().compareTo(amount) < 0) {
                createFailedTransaction(roomId, fromUserId, toUserId, amount,
                        description, "포인트가 부족합니다.");
                return false;
            }

            // 포인트 이체
            fromUser.setPoints(fromUser.getPoints().subtract(amount));
            toUser.setPoints(toUser.getPoints().add(amount));

            userRepository.save(fromUser);
            userRepository.save(toUser);

            // 성공 트랜잭션 기록
            createSuccessTransaction(roomId, fromUserId, toUserId, amount, description);

            return true;

        } catch (Exception e) {
            createFailedTransaction(roomId, fromUserId, toUserId, amount,
                    description, e.getMessage());
            return false;
        }
    }

    /**
     * 성공한 거래 기록 생성
     */
    private void createSuccessTransaction(Long roomId, Long fromUserId, Long toUserId,
                                          BigDecimal amount, String description) {
        PaymentTransaction transaction = new PaymentTransaction(
                roomId, fromUserId, toUserId, amount, description
        );
        transaction.markAsSuccess();
        paymentTransactionRepository.save(transaction);
    }

    /**
     * 실패한 거래 기록 생성
     */
    private void createFailedTransaction(Long roomId, Long fromUserId, Long toUserId,
                                         BigDecimal amount, String description, String errorMessage) {
        PaymentTransaction transaction = new PaymentTransaction(
                roomId, fromUserId, toUserId, amount, description
        );
        transaction.markAsFailed(errorMessage);
        paymentTransactionRepository.save(transaction);
    }

    /**
     * 결제 설명 생성
     */
    private String generatePaymentDescription(Room room) {
        return String.format("%s → %s 카풀비",
                room.getStartLocation(), room.getEndLocation());
    }

    /**
     * 📊 사용자별 포인트 조회
     */
    public BigDecimal getUserPoints(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        return user.getPoints();
    }

    /**
     * 💰 포인트 충전 (관리자용)
     */
    @Transactional
    public boolean chargePoints(Long userId, BigDecimal amount) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

            user.setPoints(user.getPoints().add(amount));
            userRepository.save(user);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 결제 결과 클래스
     */
    public static class PaymentResult {
        private boolean success;
        private String message;

        private PaymentResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public static PaymentResult success(String message) {
            return new PaymentResult(true, message);
        }

        public static PaymentResult failure(String message) {
            return new PaymentResult(false, message);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }
    }
}