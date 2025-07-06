## CodIN Init API Module

### 향후 MSA 환경에서 각 서비스별 API 모듈의 기본 초안

- 패키지 이름 변경 및 Gradle 수정 필수
- .env파일을 통해서 환경변수 관리

---

### 1. 인증 구조
JWT 토큰 발급: 로그인 시 서버에서 JWT 토큰을 생성하여 클라이언트에 전달합니다.
JWT 토큰 검증: 클라이언트가 API 요청 시 Authorization 헤더에 JWT 토큰을 담아 전송합니다.
필터에서 인증 처리: 커스텀 필터(예: JwtAuthenticationFilter)가 요청의 헤더에서 JWT를 추출하고, 유효성 검증 후 인증 정보를 SecurityContext에 저장합니다.
2. 주요 위치
JwtAuthenticationFilter
JWT 토큰을 파싱하고, 유효하면 인증 객체를 생성하여 SecurityContext에 저장합니다.
이 필터는 SecurityConfig의 addFilterBefore 또는 addFilter로 등록됩니다.


### SecurityConfig

기본적으로 Swagger를 제외한 모든 API 요청에 USER 요청이 들어가있습니다.

별도로 설정하기 위해서 '@PreAuthorize'를 활용하기 바랍니다.


### 토큰 인증 정보 추출 위치

JwtAuthenticationFilter에서 아래의 두 가지 방법을 모두 구현했습니다.
1. 쿠키에서 'access_token'에서 JWT 토큰 추출
2. Authorization 헤더에서 Bearer JWT 토큰 추출

그 다음에 아래와 같은 과정을 통해서 인증정보를 저장하고, 사용합니다.
- 사용자 정보(Claims)로 'UsernamePasswordAuthenticationToken' 생성
- 'SecurityContextHolder.getContext().setAuthentication(...)'로 인증 정보 저장
- 이후 컨트롤러 등에서 'SecurityUtils'를 통해 인증된 사용자 정보(사용자 이메일)를 사용할 수 있습니다.
