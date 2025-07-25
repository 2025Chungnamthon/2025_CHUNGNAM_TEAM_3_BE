# DaeCar Room API

방 생성 및 참여 기능을 제공하는 API입니다. Redis를 사용한 캐싱과 RabbitMQ를 사용한 메시지 큐 기능을 포함합니다.

## 기술 스택

- Spring Boot 3.5.3
- MySQL 8.0
- Redis 7
- RabbitMQ 3
- Docker & Docker Compose

## 실행 방법

### Docker Compose 사용

```bash
# 모든 서비스 실행
docker-compose up -d

# 로그 확인
docker-compose logs -f app

# 서비스 종료
docker-compose down
```

### 로컬 개발 환경

```bash
# MySQL, Redis, RabbitMQ 실행
docker-compose up -d mysql redis rabbitmq

# 애플리케이션 실행
./gradlew bootRun
```

## API 엔드포인트

### 1. 방 생성

```http
POST /api/rooms
Content-Type: application/json

{
  "name": "테스트 방",
  "maxParticipants": 4
}
```

### 2. 방 참여

```http
POST /api/rooms/join
Content-Type: application/json

{
  "roomId": 1,
  "userId": "user123"
}
```

### 3. 사용 가능한 방 목록 조회

```http
GET /api/rooms/available
```

### 4. 방 정보 조회

```http
GET /api/rooms/{roomId}
```

## 데이터베이스 스키마

### rooms 테이블
- `id`: Primary Key
- `name`: 방 이름
- `max_participants`: 최대 참여자 수
- `current_participants`: 현재 참여자 수
- `created_at`: 생성 시간
- `updated_at`: 수정 시간
- `is_active`: 활성 상태

### room_participants 테이블
- `id`: Primary Key
- `room_id`: 방 ID (FK)
- `user_id`: 사용자 ID
- `joined_at`: 참여 시간
- `is_active`: 활성 상태

## Redis 캐시

- `room:{roomId}`: 방 정보 캐시 (1시간)
- `room:participants:{roomId}`: 방 참여자 목록 (1시간)

## RabbitMQ 메시지

### Exchange
- `room.exchange`: Topic Exchange

### Queues & Routing Keys
- `room.created.queue` (routing key: `room.created`): 방 생성 이벤트
- `room.joined.queue` (routing key: `room.joined`): 방 참여 이벤트

## 포트 정보

- Application: 8080
- MySQL: 3306
- Redis: 6379
- RabbitMQ: 5672 (AMQP), 15672 (Management UI)

## 관리 UI

- RabbitMQ Management: http://localhost:15672 (guest/guest)