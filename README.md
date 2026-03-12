# CodIN Backend Monorepo

> 인천대학교 정보기술대학 구성원을 위한 커뮤니티 플랫폼 백엔드

학생 간 네트워킹, 강의 정보 검색, 실시간 채팅, 티켓 예매를 제공하는 모바일 앱 서비스입니다.

## 주요 화면

<p align="center">
  <img src="https://github.com/user-attachments/assets/28f628fa-47c6-4824-af7d-6b564d31ef19" width="18%" />
  <img src="https://github.com/user-attachments/assets/61fc41cb-b455-4aac-942d-a5ddcd3066bb" width="18%" />
  <img src="https://github.com/user-attachments/assets/5230be1b-5c5f-4333-b2e9-b08ed6347503" width="18%" />
  <img src="https://github.com/user-attachments/assets/4b2dc977-2745-4e3d-b35e-d93f5f285d38" width="18%" />
  <img src="https://github.com/user-attachments/assets/dc44e692-ada0-4b5b-b69a-79e279544010" width="18%" />
</p>

## 아키텍처

<!-- 아키텍처 이미지 추가 예정 -->

## 핵심 기술

- **강의 AI 요약** — Spring AI + OpenAI로 강의계획서 자동 요약, Elasticsearch 기반 검색
- **실시간 알림** — SSE + Redis Pub/Sub로 다중 클라이언트 간 동기화 지연 45ms 이내
- **실시간 채팅** — WebSocket(STOMP) 기반 1:1 채팅
- **티켓 예매** — Redis 기반 재고 동시성 제어, Quartz 스케줄링

## 기술 스택

| 영역 | 기술 |
|------|------|
| Language | Java 17 |
| Framework | Spring Boot 3.3 / 3.5, Spring Security, Spring AI |
| Database | MongoDB, MySQL, Redis, Elasticsearch 8 |
| Infra | Docker, AWS S3, Grafana/Prometheus |
| Communication | WebSocket(STOMP), SSE, OpenFeign |
| Etc | QueryDSL, Quartz, Swagger, FCM |

## 모듈 구조

| 모듈 | 설명 |
|------|------|
| [`codin-core`](codin-core/) | 커뮤니티 핵심 — 게시판, 채팅, 알림, 사용자 관리 |
| [`codin-lecture-api`](codin-lecture-api/) | 강의 정보 검색 및 AI 요약 |
| [`codin-ticketing-api`](codin-ticketing-api/) | 티켓 예매 및 관리 |
| [`codin-ticketing-sse`](codin-ticketing-sse/) | SSE 기반 실시간 알림 스트리밍 |
| [`codin-auth`](codin-auth/) | OAuth2, Apple 로그인, JWT 토큰 관리 |
| [`codin-security`](codin-security/) | Spring Security 공통 설정 |
| [`codin-common`](codin-common/) | 공유 유틸리티, 예외 처리, 공통 응답 |

## 팀 구성

| 역할 | 인원 |
|------|------|
| 기획/디자인 | 5명 |
| Frontend | 5명 |
| Backend | 6명 |

## Getting Started

```bash
# 인프라 실행
docker-compose -f docker/docker-compose.yml up -d

# 빌드
./gradlew clean build

# 실행 (각 서비스 모듈)
./gradlew :codin-core:bootRun
./gradlew :codin-lecture-api:bootRun
./gradlew :codin-ticketing-api:bootRun
./gradlew :codin-ticketing-sse:bootRun
```

## 관련 링크

- [CodIN Organization](https://github.com/CodIN-INU)
- [Frontend Repository](https://github.com/CodIN-INU/front-end)
- [Swagger](https://codin.inu.ac.kr/api/swagger-ui/index.html)
