# CodIN Ticketing API

인천대학교 정보기술대학 티켓팅 시스템을 위한 Spring Boot REST API

## 프로젝트 개요

CodIN Ticketing API는 인천대학교 정보기술대학의 다양한 이벤트(간식 나눔, 행사 등)에 대한 티켓팅 시스템을 제공 서버입니다.

### 주요 기능

- **이벤트 관리**: 티켓팅 이벤트 생성, 조회, 수정, 삭제
- **사용자 프로필**: 수령자 정보 관리 (학과, 학번)
- **티켓팅 참여**: 실시간 티켓팅 참여 및 교환권 발급
- **전자 서명**: 수령 확인을 위한 전자 서명 기능
- **관리자 기능**: 이벤트 관리, 수령 확인, 통계
- **엑셀 다운로드**: 참여자 정보 엑셀 내보내기

## 기술 스택

- **Backend**: Spring Boot 3.x, Spring Security, Spring Data JPA
- **Database**: MySQL 8.0
- **Cache**: Redis
- **Documentation**: Swagger/OpenAPI 3
- **Build Tool**: Gradle
- **Authentication**: JWT
- **Container**: Docker

## 데이터 모델

### 주요 엔티티

- **Event**: 티켓팅 이벤트 정보
- **Participation**: 이벤트별 참여자 정보 및 수령 상태
- **Stock**: 티켓팅 이벤트 재고 정보

**Enum Class**
  - **Campus**: 캠퍼스 구분 (송도캠퍼스, 미추홀캠퍼스)
  - **Department**: 학과 정보 (컴퓨터공학부, 정보통신공학과 등)

### 전제 조건

- Java 17 이상
- Docker & Docker Compose
- MySQL 8.0
- Redis

### 개발 환경 설정

- **도커 이미지 실행**
  - `./docker/codin-db/docker-compose.yml` : MySQL, Redis 실행 도커 컴포즈 스크립트

- **환경 변수 설정**
  ```bash
  # .env.example 파일을 복사하고 필요한 값들을 수정
  cp .env.example .env
  ```

## 인증 및 권한

- **JWT 토큰** 기반 인증
  - JWT 토큰에서 User Email 추출 -> `SecurityContextHolder`
  - 유저 검증 과정에서 `UserClientService`를 호출해야함.
- **역할 기반 접근 제어**:
  - `USER`: 일반 사용자 - 이벤트 조회, 티켓팅 참여
  - `MANAGER`: 관리자 - 이벤트 관리, 수령 확인
  - `ADMIN`: 최고 관리자 - 모든 권한


## 빌드 및 배포

### 1. Buildx 멀티플랫폼 빌더 생성 및 활성화 (최초 1회)

```bash
# 멀티플랫폼 빌더 생성 후 활성화
docker buildx create --name multi-builder --use

# 빌더 상태 확인 및 부트스트랩
docker buildx inspect multi-builder --bootstrap
```

### 2. Ticketing-API 이미지 빌드 & tar.gz 추출
```bash
# 1) AMD64 전용 이미지 빌드 후 로컬 데몬에 바로 로드
docker buildx build \
--platform linux/amd64 \
--load \
-t codin-ticketing-api:latest \
.

# 2) 이미지 tar로 저장
docker save codin-ticketing-api:latest -o codin-ticketing-api-amd64.tar

# 3) gzip 압축
gzip codin-ticketing-api-amd64.tar   # → codin-ticketing-api-amd64.tar.gz
```

### 3. Ticketing-SSE 이미지 빌드 & tar.gz 추출

```bash
# 1) AMD64 전용 이미지 빌드 후 로드
cd codin-ticketing-sse

docker buildx build \
  --platform linux/amd64 \
  --load \
  -t codin-ticketing-sse:latest \
  .

# 2) 이미지 tar로 저장
docker save codin-ticketing-sse:latest -o codin-ticketing-sse-amd64.tar

# 3) gzip 압축
gzip codin-ticketing-sse-amd64.tar    # → codin-ticketing-sse-amd64.tar.gz
```