package Team3rd.DaeCar.DaeCar.domain.pay.service;

import Team3rd.DaeCar.DaeCar.domain.pay.entity.PaymentTransaction;
import Team3rd.DaeCar.DaeCar.domain.pay.repository.PaymentTransactionRepository;
import Team3rd.DaeCar.DaeCar.domain.room.entity.Room;
import Team3rd.DaeCar.DaeCar.domain.room.entity.RoomParticipant;
import Team3rd.DaeCar.DaeCar.domain.room.repository.RoomParticipantRepository;
import Team3rd.DaeCar.DaeCar.domain.room.repository.RoomRepository;
import Team3rd.DaeCar.DaeCar.domain.room.service.RoomService;
import Team3rd.DaeCar.DaeCar.domain.user.entity.User;
import Team3rd.DaeCar.DaeCar.domain.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

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

    @Autowired
    private RoomService roomService;

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

            // 2. 🔥 수정된 부분: 미결제 승객들만 조회
            List<RoomParticipant> unpaidPassengers = roomParticipantRepository
                    .findUnpaidPassengersByRoomId(roomId);

            if (unpaidPassengers.isEmpty()) {
                room.setStatus(Room.RoomStatus.COMPLETED);
                roomRepository.save(room);
                return PaymentResult.success("이미 모든 결제가 완료되었습니다.");
            }

            // 3. 🔥 네이버맵 기반 1인당 결제 금액 계산
            BigDecimal perPersonCost = roomService.getCurrentPerPersonCost(roomId);

            if (perPersonCost.compareTo(BigDecimal.ZERO) <= 0) {
                return PaymentResult.failure("결제 금액을 계산할 수 없습니다.");
            }

            // 4. 각 승객이 운전자에게 결제
            int successCount = 0;
            StringBuilder resultMessage = new StringBuilder();

            for (RoomParticipant passenger : unpaidPassengers) {
                Long passengerUserId = passenger.getUserId();
                boolean paymentSuccess = transferPoints(
                        passengerUserId,
                        driverUserId,
                        perPersonCost,
                        roomId,
                        generatePaymentDescription(room.getDepartureLocation(),
                                room.getDestination(), perPersonCost)
                );

                if (paymentSuccess) {
                    passenger.setIsPaid(true);
                    roomParticipantRepository.save(passenger);
                    successCount++;
                } else {
                    // 실패한 사용자 정보 추가
                    User failedUser = userRepository.findById(passengerUserId).orElse(null);
                    if (failedUser != null) {
                        resultMessage.append(String.format("⚠️ %s님 결제 실패 (포인트 부족), ",
                                failedUser.getNickname()));
                    }
                }
            }

            // 5. 방 상태를 완료로 변경
            room.setStatus(Room.RoomStatus.COMPLETED);
            roomRepository.save(room);

            // 6. 결과 메시지 생성
            String finalMessage = String.format("🎉 카풀 완료! %d명 중 %d명 결제 성공 (1인당 %,d원)",
                    unpaidPassengers.size(), successCount, perPersonCost.intValue());

            if (resultMessage.length() > 0) {
                finalMessage += "\n" + resultMessage.toString();
            }

            return PaymentResult.success(finalMessage);

        } catch (Exception e) {
            return PaymentResult.failure("결제 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 💰 1인당 비용 계산 (간단 버전)
     */
    private BigDecimal calculatePerPersonCost(Room room, int passengerCount) {
        if (room.getTotalCost() == null || passengerCount <= 0) {
            return BigDecimal.ZERO;
        }

        return room.getTotalCost()
                .divide(BigDecimal.valueOf(passengerCount), RoundingMode.HALF_UP);
    }

    /**
     * 📊 결제 전 비용 미리보기

    public PaymentPreview getPaymentPreview(Long roomId) {
        try {
            RoomService.costInfo = roomService.getRoomCostInfo(roomId);

            return new PaymentPreview(
                    costInfo.getTotalCost(),
                    costInfo.getPerPersonCost(),
                    costInfo.getCurrentPassengers(),
                    costInfo.getDistance(),
                    costInfo.getDuration(),
                    generatePaymentDescription(costInfo.getDepartureLocation(),
                            costInfo.getDestination(), costInfo.getPerPersonCost())
            );
        } catch (Exception e) {
            return null;
        }
    }
     */
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
                        description, String.format("포인트 부족 (보유: %,d원, 필요: %,d원)",
                                fromUser.getPoints().intValue(), amount.intValue()));
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
     * 📝 결제 설명 생성
     */
    private String generatePaymentDescription(String departure, String destination, BigDecimal amount) {
        return String.format("🚗 %s → %s 카풀비 (%,d원)",
                departure, destination, amount.intValue());
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
                room.getDepartureLocation(), room.getDestination());
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

    /**
     * 결제 미리보기 클래스
     */
    public static class PaymentPreview {
        private BigDecimal totalCost;
        private BigDecimal perPersonCost;
        private int passengerCount;
        private String distance;
        private String duration;
        private String description;

        public PaymentPreview(BigDecimal totalCost, BigDecimal perPersonCost, int passengerCount,
                              String distance, String duration, String description) {
            this.totalCost = totalCost;
            this.perPersonCost = perPersonCost;
            this.passengerCount = passengerCount;
            this.distance = distance;
            this.duration = duration;
            this.description = description;
        }

        // getters
        public BigDecimal getTotalCost() { return totalCost; }
        public BigDecimal getPerPersonCost() { return perPersonCost; }
        public int getPassengerCount() { return passengerCount; }
        public String getDistance() { return distance; }
        public String getDuration() { return duration; }
        public String getDescription() { return description; }
    }
}