# 백엔드 레포(DaeCar_BE)에서 실행하는 개발용 도커 스크립트

INFRA_PATH="../DaeCar_INFRA/docker-compose"
COMPOSE_FILE="$INFRA_PATH/docker-compose.dev.yml"
IMAGE_NAME="daecar/backend:dev"

echo "🚀 DaeCar 개발용 도커 환경을 실행합니다..."

# 1. docker-compose.dev.yml 존재 확인
if [ ! -f "$COMPOSE_FILE" ]; then
  echo "❌ docker-compose.dev.yml 파일을 찾을 수 없습니다: $COMPOSE_FILE"
  exit 1
fi

# 2. 백엔드 이미지 빌드 (현재 디렉토리 기준)
echo "🔨 백엔드 Docker 이미지를 빌드합니다..."
docker build -t "$IMAGE_NAME" . || {
  echo "❌ Docker 이미지 빌드 실패"
  exit 1
}
echo "✅ 빌드 완료: $IMAGE_NAME"

# 3. docker-compose 실행
echo "🐳 Docker Compose로 인프라 및 백엔드 서비스를 실행합니다..."
docker-compose -f "$COMPOSE_FILE" up -d

# 4. 정보 출력
echo ""
echo "🎉 DaeCar 개발 서버가 실행되었습니다!"
echo ""
echo "📍 접속 정보:"
echo "- 백엔드 API: http://localhost:8080"
echo "- RabbitMQ 관리 페이지: http://localhost:15672"
echo ""
echo "🛠️ 종료하려면:"
echo "cd $INFRA_PATH && docker-compose -f docker-compose.dev.yml down"ILE down"