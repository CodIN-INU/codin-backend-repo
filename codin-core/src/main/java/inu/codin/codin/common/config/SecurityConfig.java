package inu.codin.codin.common.config;

/*
 * IMPORTANT: 이 SecurityConfig는 임시로 주석 처리됨
 * 
 * Phase 1에서 codin-security 모듈로 Resource Server 기능이 분리됨
 * Phase 2에서 codin-auth 모듈로 Authorization Server 기능을 분리할 예정
 * 
 * 현재 상태: 
 * - JWT 검증 기능 -> codin-security 모듈로 이동 완료
 * - OAuth2 로그인 기능 -> codin-core에 잠시 남아있음 (Phase 2에서 codin-auth로 이동 예정)
 * 
 * 임시 조치: 전체 설정을 주석 처리하여 컴파일 에러 방지
 * codin-core는 이제 codin-security 모듈에 의존하여 JWT 검증 기능 사용
 */

// TODO: Phase 2에서 codin-auth로 Authorization Server 기능 이동

/*
 * 기존 SecurityConfig의 역할:
 * 1. Authorization Server - OAuth2 로그인 처리 (Google, Apple)
 * 2. Resource Server - JWT 토큰 검증 및 권한 체크
 * 
 * 분리 후:
 * 1. Authorization Server -> codin-auth 모듈 (Phase 2에서 구현)
 * 2. Resource Server -> codin-security 모듈 (Phase 1에서 완료)
 */