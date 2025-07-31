# CodIN Ticketing API

<img width="1324" height="679" alt="image" src="https://github.com/user-attachments/assets/d3719539-a47e-4043-a57a-408570411762" />

인천대학교 정보기술대학 티켓팅 시스템을 위한 Spring Boot REST API

## 프로젝트 개요

**CodIN Ticketing API** 는 인천대학교 정보기술대학의 다양한 **이벤트(간식 나눔, 행사 등)** 에 대한 **티켓팅 시스템** 을 제공 서버입니다.

### 주요 기능

<img width="812" height="511" alt="image" src="https://github.com/user-attachments/assets/1499b0b8-bc7c-44c6-8baa-f51aee8bab29" />

[API Endpoint 문서](/API.md)

- **이벤트 관리**: 티켓팅 이벤트 생성, 조회, 수정, 삭제
- **사용자 프로필**: 수령자 정보 관리 (학과, 학번)
- **티켓팅 참여**: 실시간 티켓팅 참여 및 교환권 발급
- **전자 서명**: 수령 확인을 위한 전자 서명 기능
- **관리자 기능**: 이벤트 관리, 수령 확인, 통계
- **엑셀 다운로드**: 참여자 정보 엑셀 내보내기

## 기술 스택

- **Backend**: Spring Boot 3.5.3, Spring Security, Spring Data JPA, SSE
- **Database**: MySQL 8.0
- **MQ**: Redis Stream
- **Authentication**: JWT

## 데이터 모델

<img width="2094" height="1186" alt="스크린샷 2025-07-24 18 12 10" src="https://github.com/user-attachments/assets/f124eb08-ffc9-4411-afbf-b56238273c01" />

### 주요 엔티티

- **Event**: 티켓팅 이벤트 정보
- **Participation**: 이벤트별 참여자 정보 및 수령 상태
- **Stock**: 티켓팅 이벤트 재고 정보

**Enum Class**
  - **Campus**: 캠퍼스 구분 (송도캠퍼스, 미추홀캠퍼스)
  - **Department**: 학과 정보 (컴퓨터공학부, 정보통신공학과 등)

## 인증 및 권한

- **JWT 토큰 기반 인증**
  - JWT 토큰에서 User Email 추출 -> `SecurityContextHolder`
  - SecurityUtils 클래스로 SecurityContextHolder 사용
  - 유저 검증 과정에서 `UserClientService`를 호출해야함.
  - User Role:
    - `USER`: 일반 사용자 - 이벤트 조회, 티켓팅 참여
    - `MANAGER`: 관리자 - 이벤트 관리, 수령 확인
    - `ADMIN`: 최고 관리자 - 모든 권한

## 개발 환경 설정

- **도커 이미지 실행**
  - `./docker/codin-db/docker-compose.yml` : MySQL, Redis 실행 도커 컴포즈 스크립트

- **환경 변수 설정**
  ```bash
  # .env.example 파일을 복사해 필요한 값들을 수정
  cp .env.example .env
  ```

