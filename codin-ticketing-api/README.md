# CodIN Ticketing API

🎫 인천대학교 정보기술대학 티켓팅 시스템을 위한 Spring Boot REST API

## 📋 프로젝트 개요

CodIN Ticketing API는 인천대학교 정보기술대학의 다양한 이벤트(간식 나눔, 행사 등)에 대한 티켓팅 시스템을 제공하는 Spring Boot 기반의 REST API입니다.

### 주요 기능

- 🎫 **이벤트 관리**: 티켓팅 이벤트 생성, 조회, 수정, 삭제
- 👥 **사용자 프로필**: 수령자 정보 관리 (학과, 학번)
- 🎯 **티켓팅 참여**: 실시간 티켓팅 참여 및 교환권 발급
- ✍️ **전자 서명**: 수령 확인을 위한 전자 서명 기능
- 📊 **관리자 기능**: 이벤트 관리, 수령 확인, 통계
- 📄 **엑셀 다운로드**: 참여자 정보 엑셀 내보내기

## 🏗️ 기술 스택

- **Backend**: Spring Boot 3.x, Spring Security, Spring Data MongoDB
- **Database**: MongoDB
- **Cache**: Redis
- **Documentation**: Swagger/OpenAPI 3
- **Build Tool**: Gradle
- **Authentication**: JWT
- **Container**: Docker

## 🏛️ 아키텍처

```
src/main/java/inu/codin/codinticketingapi/
├── common/                    # 공통 모듈
│   ├── config/               # 설정 클래스
│   ├── exception/            # 전역 예외 처리
│   ├── response/             # 공통 응답 형식
│   ├── security/             # 보안 관련
│   └── util/                 # 유틸리티 클래스
└── domain/
    └── ticketing/            # 티켓팅 도메인
        ├── controller/       # REST 컨트롤러
        ├── dto/             # 데이터 전송 객체
        ├── entity/          # 도메인 엔티티
        ├── exception/       # 도메인 예외
        ├── repository/      # 데이터 접근 계층
        └── service/         # 비즈니스 로직
```

## 데이터 모델

### 주요 엔티티

- **TicketingEvent**: 티켓팅 이벤트 정보
- **TicketingProfile**: 사용자 수령자 정보 (학과, 학번)
- **TicketingInfo**: 이벤트별 참여자 정보 및 수령 상태
- **Campus**: 캠퍼스 구분 (송도캠퍼스, 미추홀캠퍼스)
- **Department**: 학과 정보 (컴퓨터공학부, 정보통신공학과 등)

## 시작하기

### 로컬 개발 환경 설정

1. **프로젝트 클론**
```bash
git clone [repository-url]
cd codin-ticketing-api
```

2. **Docker 컨테이너 실행**
```bash
docker-compose up -d
```

3. **애플리케이션 실행**
```bash
./gradlew bootRun
```

4. **API 문서 확인**
```
http://localhost:8080/swagger-ui.html
```

## 📡 API 엔드포인트

### 이벤트 관리

```http
GET    /ticketing/events                    # 이벤트 목록 조회
GET    /ticketing/events/{eventId}          # 이벤트 상세 조회
GET    /ticketing/events/management         # 관리자용 이벤트 목록
GET    /ticketing/events/{eventId}/password # 이벤트 비밀번호 조회 (관리자)
POST   /ticketing/events/{eventId}/close    # 이벤트 마감 (관리자)
PUT    /ticketing/events/{eventId}          # 이벤트 수정 (관리자)
DELETE /ticketing/events/{eventId}          # 이벤트 삭제 (관리자)
```

### 티켓팅 참여

```http
POST   /ticketing/events/{eventId}/join     # 티켓팅 참여
POST   /ticketing/events/{eventId}/confirm  # 수령 확인 (관리자)
POST   /ticketing/events/{eventId}/signature # 전자 서명 업로드
```

### 사용자 프로필

```http
GET    /ticketing/user-profile              # 수령자 정보 조회
POST   /ticketing/user-profile              # 수령자 정보 등록
```

### 엑셀 다운로드

```http
GET    /ticketing/excel/{eventId}           # 참여자 정보 엑셀 다운로드 (관리자)
```

## 빌드 및 배포

```bash
# JAR 파일 빌드
./gradlew build

# Docker 이미지 빌드
docker build -t codin-ticketing-api .

# Docker 컨테이너 실행
docker run -p 8080:8080 codin-ticketing-api
```

---

**CodIN Ticketing API** - 인천대학교 정보기술대학 티켓팅 시스템 🎫
