# codin-auth

> 인증/인가 서비스 — OAuth2, Apple 로그인, JWT 토큰 관리

## 주요 기능

- Apple OAuth2 소셜 로그인
- JWT 기반 토큰 발급/갱신/검증
- 회원가입 및 프로필 완성 플로우
- 관리자 로그인

## 패키지 구조

| 패키지 | 설명 |
|--------|------|
| `controller` | Auth, Apple Event API |
| `service/oauth2` | OAuth2 인증 처리 |
| `jwt` | JWT 토큰 생성/파싱 |
| `handler` | 인증 성공/실패 핸들러 |
| `feign` | 외부 서비스 연동 |

## 기술 스택

Spring Boot 3.5, Spring Security, OAuth2, JWT, OpenFeign
