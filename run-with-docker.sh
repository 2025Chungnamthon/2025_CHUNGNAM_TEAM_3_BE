#!/bin/bash

# DaeCar 전체 서비스 Docker 실행 스크립트
# 백엔드 앱도 포함해서 모든 서비스를 Docker로 실행합니다.

INFRA_PATH="../DaeCar_INFRA/docker-compose"
IMAGE_NAME="daecar/backend:latest"

echo "🚀 DaeCar 전체 서비스를 Docker로 실행합니다..."

# 인프라 폴더 존재 확인
if [ ! -d "$INFRA_PATH" ]; then
    echo "❌ DaeCar_INFRA 폴더를 찾을 수 없습니다."
    echo "   경로: $INFRA_PATH"
    exit 1
fi

# 1단계: 백엔드 이미지 빌드
echo "🔨 백엔드 Docker 이미지를 빌드합니다..."
docker build -t $IMAGE_NAME . || {
    echo "❌ Docker 이미지 빌드에 실패했습니다."
    exit 1
}

echo "✅ 이미지 빌드 완료: $IMAGE_NAME"

# 2단계: .env 파일 확인 및 생성
cd "$INFRA_PATH"

if [ ! -f ".env" ]; then
    echo "📝 .env 파일을 생성합니다..."
    cp .env.example .env
    
    # 이미지 이름 자동 설정
    sed -i.bak "s|DAECAR_BACKEND_IMAGE=.*|DAECAR_BACKEND_IMAGE=$IMAGE_NAME|g" .env
    rm .env.bak 2>/dev/null
    
    echo "✅ .env 파일이 생성되었습니다."
else
    # 기존 .env 파일의 이미지 이름 업데이트
    if grep -q "DAECAR_BACKEND_IMAGE" .env; then
        sed -i.bak "s|DAECAR_BACKEND_IMAGE=.*|DAECAR_BACKEND_IMAGE=$IMAGE_NAME|g" .env
        rm .env.bak 2>/dev/null
    else
        echo "DAECAR_BACKEND_IMAGE=$IMAGE_NAME" >> .env
    fi
fi

# 3단계: Docker Compose로 모든 서비스 실행
echo "🐳 모든 서비스를 시작합니다..."
docker-compose -f docker-compose.prod.yml up -d

echo ""
echo "⏳ 서비스들이 준비될 때까지 기다립니다..."
sleep 15

# 4단계: 서비스 상태 확인
echo "🔍 서비스 상태를 확인합니다..."
docker-compose -f docker-compose.prod.yml ps

echo ""
echo "🌐 백엔드 서비스 헬스체크..."
for i in {1..30}; do
    if curl -f http://localhost:8080/actuator/health &> /dev/null; then
        echo "✅ 백엔드 서비스가 정상적으로 시작되었습니다!"
        break
    elif [ $i -eq 30 ]; then
        echo "❌ 백엔드 서비스 시작에 실패했습니다 (30초 타임아웃)"
        echo "로그를 확인해보세요: docker-compose -f docker-compose.prod.yml logs daecar-backend"
    else
        echo -n "."
        sleep 1
    fi
done

echo ""
echo "🎉 DaeCar 서비스가 모두 실행되었습니다!"
echo ""
echo "📍 서비스 접속 정보:"
echo "- 백엔드 API: http://localhost:8080"
echo "- RabbitMQ 관리페이지: http://localhost:15672"
echo ""
echo "🛠️ 유용한 명령어:"
echo "- 전체 로그 보기: cd $INFRA_PATH && docker-compose -f docker-compose.prod.yml logs -f"
echo "- 백엔드 로그만: cd $INFRA_PATH && docker-compose -f docker-compose.prod.yml logs -f daecar-backend"
echo "- 서비스 중지: cd $INFRA_PATH && docker-compose -f docker-compose.prod.yml down"
echo ""
echo "🧪 API 테스트:"
echo 'curl -X POST http://localhost:8080/api/rooms -H "Content-Type: application/json" -d '"'"'{"name": "테스트 방", "maxParticipants": 4}'"'"
echo ""