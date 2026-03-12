# codin-core

> CodIN 커뮤니티 핵심 서비스 — 게시판, 채팅, 알림, 사용자 관리

## 주요 화면

<p align="center">
  <img src="https://github.com/user-attachments/assets/28f628fa-47c6-4824-af7d-6b564d31ef19" width="18%" />
  <img src="https://github.com/user-attachments/assets/61fc41cb-b455-4aac-942d-a5ddcd3066bb" width="18%" />
  <img src="https://github.com/user-attachments/assets/5230be1b-5c5f-4333-b2e9-b08ed6347503" width="18%" />
  <img src="https://github.com/user-attachments/assets/4b2dc977-2745-4e3d-b35e-d93f5f285d38" width="18%" />
  <img src="https://github.com/user-attachments/assets/dc44e692-ada0-4b5b-b69a-79e279544010" width="18%" />
</p>

## 주요 기능

- 게시판 CRUD, 댓글/대댓글, 익명 투표, 베스트 게시글 선정
- WebSocket(STOMP) 기반 1:1 실시간 채팅
- FCM 기반 푸시 알림 (댓글, 좋아요, 채팅 등)

## 도메인 구조

| 도메인 | 설명 |
|--------|------|
| `post` | 게시글 CRUD, 댓글/대댓글, 투표, 조회수, 베스트 |
| `board` | 공지사항(notice), 질문(question), 익명(voice) |
| `chat` | 채팅방 관리 + 메시지 처리 |
| `user` | 사용자 프로필, 내부 API |
| `like` | 좋아요 |
| `notification` | 알림 관리 |
| `email` | 웹메일 인증 |
| `report` | 신고 처리 + 자동 스케줄링 |
| `calendar` | 학사 일정 |
| `scrap` | 게시글 스크랩 |
| `block` | 사용자 차단 |

## 인프라 계층

| 패키지 | 설명 |
|--------|------|
| `infra/fcm` | Firebase Cloud Messaging 푸시 알림 |
| `infra/redis` | 캐시, 세션 관리, 스케줄러 |
| `infra/s3` | AWS S3 파일 업로드 |

## 기술 스택

Spring Boot 3.3, MongoDB, Redis, WebSocket(STOMP), FCM, AWS S3, OpenFeign
