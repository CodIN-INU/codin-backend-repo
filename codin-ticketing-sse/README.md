# codin-ticketing-sse

> SSE 기반 실시간 알림 스트리밍 서비스

`codin-ticketing-api`에서 발생하는 재고 변동 이벤트를 클라이언트에 실시간으로 전달합니다.
티켓 API와 알림 스트리밍의 관심사를 분리하여 독립적인 스케일링과 장애 격리를 확보했습니다.

## 주요 화면

<!-- 스크린샷 추가 예정 -->

## 주요 기능

- Server-Sent Events(SSE) 기반 실시간 티켓 재고/상태 알림
- Redis Pub/Sub 기반 다중 인스턴스 간 이벤트 동기화
- SSE 연결 상태 모니터링 및 자동 재연결
- WebFlux 기반 비동기 스트리밍

## 패키지 구조

| 패키지 | 설명 |
|--------|------|
| `sse/controller` | SSE 구독 엔드포인트 |
| `sse/service` | 이벤트 발행 및 구독 관리 |
| `sse/listener` | Redis Pub/Sub 이벤트 리스너 |
| `sse/repository` | SSE 연결 저장소 (SseEmitter 관리) |
| `sse/monitor` | 연결 상태 모니터링 |

## 기술 스택

Spring Boot 3.5, WebFlux, Redis Pub/Sub
