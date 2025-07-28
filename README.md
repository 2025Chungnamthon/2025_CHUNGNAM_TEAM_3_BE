# 🚗 DaeCar - 대학생 전용 카풀/택시 매칭 서비스

> **2025 충남톤 3팀** - 대학생들을 위한 스마트 카풀 & 택시 쉐어링 플랫폼

## 📋 프로젝트 소개

**DaeCar**는 대학생들이 안전하고 경제적으로 이동할 수 있도록 돕는 카풀/택시 매칭 서비스입니다. 
실시간 위치 공유, 채팅, 대학 이메일 인증을 통해 신뢰할 수 있는 이동 서비스를 제공합니다.

### ✨ 주요 기능

- **🎓 대학생 인증**: 대학 이메일을 통한 실명 인증 시스템
- **🚙 카풀/택시 매칭**: 출발지-목적지 기반 실시간 매칭
- **💬 실시간 채팅**: WebSocket 기반 그룹 채팅
- **📍 위치 공유**: 실시간 위치 추적 및 공유
- **💰 요금 정산**: 자동 요금 계산 및 포인트 시스템
- **🔐 보안**: JWT 토큰 기반 인증 및 권한 관리

## 🛠 기술 스택

### Backend
- **Framework**: Spring Boot 3.5.3 (Java 17)
- **Database**: MySQL 8.0, RDS (AWS)
- **Cache**: Redis 7.0
- **Message Queue**: RabbitMQ 3.13
- **Authentication**: JWT, Spring Security

### External APIs
- **지도/경로**: Naver Maps API
- **이메일**: Gmail SMTP
- **OCR**: Naver Clova OCR

### Infrastructure
- **Container**: Docker, Docker Compose
- **CI/CD**: Jenkins
- **Cloud**: AWS (RDS)

## 🚀 빠른 시작

### 1. 필수 요구사항

- **Java 17** 이상
- **Docker & Docker Compose**
- **Git**

### 2. 프로젝트 클론

```bash
git clone https://github.com/2025-CHUNGNAM-TEAM-3/2025_CHUNGNAM_TEAM_3_BE.git
cd 2025_CHUNGNAM_TEAM_3_BE
```

### 3. 환경 설정

#### Option A: Docker Compose (권장)

```bash
# 모든 서비스 실행 (MySQL, Redis, RabbitMQ, Application)
docker-compose up -d

# 로그 확인
docker-compose logs -f app

# 서비스 종료
docker-compose down
```

#### Option B: 로컬 개발 환경

```bash
# 1. 인프라 서비스만 실행
docker-compose up -d mysql redis rabbitmq

# 2. 애플리케이션 설정 파일 수정
# src/main/resources/application.properties에서 다음 값들을 localhost로 변경:
# spring.data.redis.host=localhost
# spring.rabbitmq.host=localhost

# 3. 애플리케이션 실행
./gradlew bootRun
```

### 4. 서비스 접속

- **애플리케이션**: http://localhost:8080
- **Swagger API 문서**: http://localhost:8080/swagger-ui.html
- **RabbitMQ 관리**: http://localhost:15672 (guest/guest)

## 📚 API 문서

### 🎓 학생 인증

```http
# 이메일 인증 코드 발송
POST /api/auth/send-code
Content-Type: application/json

{
  "email": "student@smail.kongju.ac.kr"
}

# 인증 코드 확인
POST /api/auth/verify-code
Content-Type: application/json

{
  "email": "student@smail.kongju.ac.kr",
  "code": "123456"
}
```

### 🚗 카풀/택시 방

```http
# 방 생성
POST /api/rooms
Content-Type: application/json

{
  "name": "공주대 → 서울역",
  "roomType": "CARPOOL",
  "maxParticipants": 4,
  "departureLocation": "공주대학교",
  "destination": "서울역",
  "departureTime": "2025-01-15T14:00:00",
  "costPerPerson": 15000,
  "startLatitude": 36.5184,
  "startLongitude": 127.1164,
  "endLatitude": 37.5547,
  "endLongitude": 126.9707
}

# 사용 가능한 방 목록
GET /api/rooms/available

# 카풀방만 조회
GET /api/rooms/carpool

# 택시방만 조회  
GET /api/rooms/taxi
```

### 💬 실시간 채팅

```javascript
// WebSocket 연결
const socket = new SockJS('/ws');
const stompClient = Stomp.over(socket);

// 메시지 전송
stompClient.send('/app/chat.sendMessage', {}, JSON.stringify({
  roomId: 1,
  senderId: 123,
  senderName: '홍길동',
  content: '안녕하세요!',
  messageType: 'CHAT'
}));

// 메시지 수신
stompClient.subscribe('/topic/room/1', function(message) {
  console.log(JSON.parse(message.body));
});
```

## 🗂 프로젝트 구조

```
src/main/java/Team3rd/DaeCar/DaeCar/
├── domain/
│   ├── chat/           # 실시간 채팅
│   ├── driver/         # 운전면허 OCR
│   ├── map/           # 지도/경로 API
│   ├── pay/           # 결제/포인트
│   ├── room/          # 방 생성/관리
│   ├── student/       # 학생 인증
│   └── user/          # 사용자 관리
├── global/
│   ├── config/        # 설정 (Redis, RabbitMQ, Security 등)
│   ├── security/      # JWT 인증
│   └── util/          # 유틸리티
└── DaeCarApplication.java
```

## 🎯 지원 대학

- 공주대학교 (`smail.kongju.ac.kr`)
- 백석대학교 (`bu.ac.kr`)
- 상명대학교 (`sangmyung.kr`)
- 호서대학교 (`vision.hoseo.edu`)
- 단국대학교 (`dankook.ac.kr`)

## 🔧 개발 환경 설정

### 로컬 개발용 설정

```properties
# application-local.properties
spring.profiles.active=local

# Database
spring.datasource.url=jdbc:mysql://localhost:3306/daecar
spring.datasource.username=root
spring.datasource.password=

# Redis
spring.data.redis.host=localhost
spring.data.redis.port=6379

# RabbitMQ
spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
```

### 프로덕션 환경 설정

```properties
# application.properties (현재 설정)
# RDS 연동
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}

# Redis (Docker 환경)
spring.data.redis.host=daecar-redis-prod

# RabbitMQ (Docker 환경)
spring.rabbitmq.host=daecar-rabbitmq-prod
```

## 🐳 Docker 구성

### docker-compose.yml 주요 서비스

```yaml
services:
  mysql:
    image: mysql:8.0
    ports: ["3306:3306"]
    
  redis:
    image: redis:7-alpine
    ports: ["6379:6379"]
    
  rabbitmq:
    image: rabbitmq:3.13-management
    ports: ["5672:5672", "15672:15672"]
    
  app:
    build: .
    ports: ["8080:8080"]
    depends_on: [mysql, redis, rabbitmq]
```

## 🧪 테스트

```bash
# 단위 테스트 실행
./gradlew test

# 특정 테스트 실행
./gradlew test --tests "Team3rd.DaeCar.DaeCar.domain.room.*"

# 테스트 리포트 확인
open build/reports/tests/test/index.html
```

## 📊 모니터링

### RabbitMQ 관리 UI
- URL: http://localhost:15672
- 계정: guest/guest
- 큐 상태, 메시지 플로우 모니터링 가능

### 애플리케이션 헬스체크
```http
GET /actuator/health
GET /actuator/info
```

## 🤝 기여하기

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 라이선스

이 프로젝트는 MIT 라이선스 하에 있습니다. 자세한 내용은 `LICENSE` 파일을 참조하세요.

## 👥 팀 정보

**2025 충남톤 3팀**
- Backend Developer: Spring Boot, Redis, RabbitMQ 전문
- 대학생 전용 서비스 기획 및 개발

## 📞 문의

프로젝트에 대한 문의사항이나 버그 리포트는 GitHub Issues를 이용해주세요.

---

⭐ **Star**를 눌러주시면 프로젝트 발전에 큰 힘이 됩니다!