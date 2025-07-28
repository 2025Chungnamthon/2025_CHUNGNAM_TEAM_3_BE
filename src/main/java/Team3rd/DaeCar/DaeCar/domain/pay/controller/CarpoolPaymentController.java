package Team3rd.DaeCar.DaeCar.domain.pay.controller;

import Team3rd.DaeCar.DaeCar.domain.pay.dto.*;
import Team3rd.DaeCar.DaeCar.domain.pay.entity.PaymentTransaction;
import Team3rd.DaeCar.DaeCar.domain.pay.repository.PaymentTransactionRepository;
import Team3rd.DaeCar.DaeCar.domain.pay.service.CarpoolPaymentService;
import Team3rd.DaeCar.DaeCar.domain.user.entity.User;
import Team3rd.DaeCar.DaeCar.domain.user.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/payment")
@CrossOrigin(origins = "*")
public class CarpoolPaymentController {

    @Autowired
    private CarpoolPaymentService carpoolPaymentService;

    @Autowired
    private PaymentTransactionRepository paymentTransactionRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * 🚗 카풀 도착 완료 & 자동 결제 처리
     * POST /api/payment/complete-trip
     */
    @PostMapping("/complete-trip")
    public ResponseEntity<PaymentResponse> completeTrip(@Valid @RequestBody CompleteTripRequest request) {
        try {
            CarpoolPaymentService.PaymentResult result = carpoolPaymentService.completeTrip(
                    request.getRoomId(),
                    request.getDriverUserId()
            );

            if (result.isSuccess()) {
                return ResponseEntity.ok(PaymentResponse.success(result.getMessage()));
            } else {
                return ResponseEntity.badRequest()
                        .body(PaymentResponse.failure(result.getMessage()));
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(PaymentResponse.failure("결제 처리 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    /**
     * 💰 사용자 포인트 조회
     * GET /api/payment/points/{userId}
     */
    @GetMapping("/points/{userId}")
    public ResponseEntity<PointsResponse> getUserPoints(@PathVariable Long userId) {
        try {
            BigDecimal points = carpoolPaymentService.getUserPoints(userId);

            // 사용자 닉네임도 함께 조회
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

            PointsResponse response = new PointsResponse(userId, user.getNickname(), points);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 💸 포인트 충전 (관리자용)
     * POST /api/payment/charge-points
     */
    @PostMapping("/charge-points")
    public ResponseEntity<PaymentResponse> chargePoints(@Valid @RequestBody ChargePointsRequest request) {
        try {
            boolean success = carpoolPaymentService.chargePoints(request.getUserId(), request.getAmount());

            if (success) {
                return ResponseEntity.ok(PaymentResponse.success(
                        String.format("%,d원이 성공적으로 충전되었습니다.", request.getAmount().longValue())
                ));
            } else {
                return ResponseEntity.badRequest()
                        .body(PaymentResponse.failure("포인트 충전에 실패했습니다."));
            }

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(PaymentResponse.failure(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(PaymentResponse.failure("충전 처리 중 오류가 발생했습니다."));
        }
    }

    /**
     * 📊 카풀방별 결제 내역 조회
     * GET /api/payment/history/room/{roomId}
     */
    @GetMapping("/history/room/{roomId}")
    public ResponseEntity<List<TransactionHistoryResponse>> getRoomPaymentHistory(@PathVariable Long roomId) {
        try {
            List<PaymentTransaction> transactions = paymentTransactionRepository
                    .findByRoomIdOrderByCreatedAtDesc(roomId);

            List<TransactionHistoryResponse> response = transactions.stream()
                    .map(this::convertToTransactionHistoryResponse)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 💳 사용자별 결제 내역 조회 (내가 받은 돈 - 운전자 수익)
     * GET /api/payment/history/received/{userId}
     */
    @GetMapping("/history/received/{userId}")
    public ResponseEntity<List<TransactionHistoryResponse>> getReceivedPaymentHistory(@PathVariable Long userId) {
        try {
            List<PaymentTransaction> transactions = paymentTransactionRepository
                    .findByToUserIdOrderByCreatedAtDesc(userId);

            List<TransactionHistoryResponse> response = transactions.stream()
                    .map(this::convertToTransactionHistoryResponse)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 💸 사용자별 결제 내역 조회 (내가 쓴 돈 - 승객 지출)
     * GET /api/payment/history/paid/{userId}
     */
    @GetMapping("/history/paid/{userId}")
    public ResponseEntity<List<TransactionHistoryResponse>> getPaidPaymentHistory(@PathVariable Long userId) {
        try {
            List<PaymentTransaction> transactions = paymentTransactionRepository
                    .findByFromUserIdOrderByCreatedAtDesc(userId);

            List<TransactionHistoryResponse> response = transactions.stream()
                    .map(this::convertToTransactionHistoryResponse)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 📈 특정 기간 결제 내역 조회 (관리자용)
     * GET /api/payment/history/period?startDate=2024-01-01&endDate=2024-12-31
     */
    @GetMapping("/history/period")
    public ResponseEntity<List<TransactionHistoryResponse>> getPaymentHistoryByPeriod(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        try {
            LocalDateTime start = LocalDateTime.parse(startDate + "T00:00:00");
            LocalDateTime end = LocalDateTime.parse(endDate + "T23:59:59");

            List<PaymentTransaction> transactions = paymentTransactionRepository
                    .findByCreatedAtBetween(start, end);

            List<TransactionHistoryResponse> response = transactions.stream()
                    .map(this::convertToTransactionHistoryResponse)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 💰 카풀방 총 결제 금액 조회
     * GET /api/payment/room/{roomId}/total
     */
    @GetMapping("/room/{roomId}/total")
    public ResponseEntity<PaymentResponse.PaymentData> getRoomTotalPayment(@PathVariable Long roomId) {
        try {
            Long totalAmount = paymentTransactionRepository.getTotalSuccessfulPaymentsByRoom(roomId);
            List<PaymentTransaction> transactions = paymentTransactionRepository
                    .findByRoomIdOrderByCreatedAtDesc(roomId);

            long successfulPayments = transactions.stream()
                    .filter(t -> t.getStatus() == PaymentTransaction.TransactionStatus.SUCCESS)
                    .count();

            PaymentResponse.PaymentData data = new PaymentResponse.PaymentData(
                    roomId,
                    transactions.size(),
                    (int) successfulPayments,
                    String.format("%,d원", totalAmount)
            );

            return ResponseEntity.ok(data);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 🏥 결제 상태별 조회 (관리자용)
     * GET /api/payment/history/status/{status}
     */
    @GetMapping("/history/status/{status}")
    public ResponseEntity<List<TransactionHistoryResponse>> getPaymentHistoryByStatus(
            @PathVariable String status) {
        try {
            PaymentTransaction.TransactionStatus transactionStatus =
                    PaymentTransaction.TransactionStatus.valueOf(status.toUpperCase());

            List<PaymentTransaction> transactions = paymentTransactionRepository
                    .findByStatusOrderByCreatedAtDesc(transactionStatus);

            List<TransactionHistoryResponse> response = transactions.stream()
                    .map(this::convertToTransactionHistoryResponse)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * PaymentTransaction을 TransactionHistoryResponse로 변환하는 헬퍼 메서드
     */
    private TransactionHistoryResponse convertToTransactionHistoryResponse(PaymentTransaction transaction) {
        // 사용자 닉네임들을 조회해서 응답에 포함
        String fromUserNickname = userRepository.findById(transaction.getFromUserId())
                .map(User::getNickname)
                .orElse("알 수 없는 사용자");

        String toUserNickname = userRepository.findById(transaction.getToUserId())
                .map(User::getNickname)
                .orElse("알 수 없는 사용자");

        return new TransactionHistoryResponse(transaction, fromUserNickname, toUserNickname);
    }
}