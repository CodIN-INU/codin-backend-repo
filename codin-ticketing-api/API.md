# 티켓팅 도메인 API 엔드포인트 문서

## CODIN Ticketing Module API Documentation

**Base URL:** `https://codin.inu.ac.kr/api/ticketing`

### Test API
- **GET** `/v3/api/test1` — 기본 접근 테스트 (로그인 없이 접근 가능)
- **GET** `/v3/api/test2` — 기본 접근 테스트 (로그인 데이터 확인)
- **GET** `/v3/api/test3` — 유저 권한 테스트1 (USER 권한)
- **GET** `/v3/api/test4` — 유저 권한 테스트2 (ADMIN 권한)
- **GET** `/v3/api/test5` — 유저 권한 테스트3 (MANAGER 권한)

### Event API
- **GET** `/event` - 티켓팅 이벤트 목록 조회 (송도/미추홀 캠퍼스, 페이징)
- **GET** `/event/{eventId}` - 티켓팅 이벤트 상세 정보 조회
- **GET** `/event/user` - 유저 마이페이지: 참여 전체 이력 조회 (페이징)
- **GET** `/event/user/status` - 유저 마이페이지: 상태별 이력 조회 (COMPLETED, WAITING, CANCELED)

### Event Excel API
- **GET** `/ticketing/excel/{eventId}` - 참여자 정보 엑셀 다운로드 (관리자)

### Ticketing API
- **POST** `/event/join/{eventId}` - 특정 이벤트에 티켓팅 참여 (교환권 부여)
- **DELETE** `/event/cancel/{eventId}` - 사용자 티켓팅 취소
- **POST** `/event/complete/{eventId}` - 교환권 수령 확인 및 서명 이미지 업로드 (관리자 비밀번호 필요)
- **POST** `/event/sse/{eventId}` - [테스트] 재고상태 SSE 전송 (MANAGER, ADMIN)

### Admin API
- **POST** `/admin/event/create` - 티켓팅 이벤트 생성 (multipart/form-data: eventContent, eventImage)
- **PUT** `/admin/event/{eventId}` - 티켓팅 이벤트 수정 (multipart/form-data: eventUpdateRequest, eventImage 선택)
- **DELETE** `/admin/event/{eventId}` - 티켓팅 이벤트 삭제
- **POST** `/admin/event/{eventId}/open` - 이벤트 수동 오픈
- **POST** `/admin/event/{eventId}/close` - 티켓팅 이벤트 마감
- **GET** `/admin/event/{eventId}/password` - 이벤트 비밀번호 조회
- **GET** `/admin/event/{eventId}/participation` - 이벤트 수령자 리스트 조회 (페이징)
- **GET** `/admin/event/{eventId}/management/stock` - 이벤트 잔여 수량 조회
- **GET** `/admin/event/list` - 관리자용 이벤트 리스트 조회 (상태, 페이징)
- **PUT** `/admin/event/{eventId}/management/status/{userId}` - 특정 사용자 수령 상태 변경
- **DELETE** `/admin/event/{eventId}/management/cancel/{userId}` - 특정 사용자 티켓 취소

---

## CODIN Ticketing SSE Module API Documentation

**Base URL:** `https://codin.inu.ac.kr/api/ticketing/sse`

### Stock Status SSE API
- **GET** `/{eventId}` - 이벤트 재고상태 SSE 구독
- **POST** `/{eventId}` - [테스트] 재고상태 SSE 전송 (MANAGER, ADMIN)cketing/excel/{eventId}