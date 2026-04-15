# codin-ticketing-api

> 티켓 예매 및 관리 서비스

## 주요 기능

<img width="100%" alt="주요 기능" src="https://github.com/user-attachments/assets/d3719539-a47e-4043-a57a-408570411762" />

## 아키텍처

<img width="100%" alt="아키텍처" src="https://github.com/user-attachments/assets/53a52f4c-e766-4a9d-80cc-f1d507c19413" />

## 상세 기능

- 티켓 생성/예매/취소 API
- Redis 기반 재고 관리 (동시성 제어)
- Quartz 스케줄러 기반 예매 오픈/마감 자동화
- 관리자 대시보드 (티켓 현황, Excel 다운로드)
- SSE 서비스로 재고 변동 이벤트 발행

## 도메인 구조

| 도메인 | 설명 |
|--------|------|
| `ticketing` | 티켓 예매 핵심 — 엔티티, Redis 재고, 이벤트 스트림, 스케줄러 |
| `admin` | 관리자 API — 티켓 생성, 현황 조회, Excel 추출, Quartz 스케줄링 |
| `image` | 티켓 이미지 업로드 |
| `user` | 사용자 정보 Feign 연동 |

## 데이터 모델

<img width="100%" alt="ERD" src="https://github.com/user-attachments/assets/f124eb08-ffc9-4411-afbf-b56238273c01" />

## 기술 스택

Spring Boot 3.5, MySQL, Redis, Quartz, AWS S3, OpenFeign, Spring Retry
