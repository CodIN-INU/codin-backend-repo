# codin-lecture-api

> 강의 정보 검색 및 AI 요약 서비스

## 주요 화면

<!-- 스크린샷 추가 예정 -->

## 아키텍처

<!-- 아키텍처 이미지 추가 예정 -->

## 주요 기능

- 강의계획서 데이터 크롤링 및 DB 적재
- Spring AI(OpenAI) 기반 강의별 AI 요약 자동 생성
- MySQL → Elasticsearch 배치 동기화 파이프라인
- Elasticsearch 기반 강의 검색 (자동완성, 필터링)
- 강의 리뷰 및 좋아요

## 도메인 구조

| 도메인 | 설명 |
|--------|------|
| `lecture` | 강의 엔티티, 크롤링 스케줄러, 조회 API |
| `elasticsearch` | ES 문서 매핑, 인덱서, 검색 서비스, 배치 동기화 |
| `review` | 강의 리뷰 CRUD |
| `like` | 강의 좋아요 |
| `user` | 사용자 정보 Feign 연동 |

## 기술 스택

Spring Boot 3.5, MySQL, Elasticsearch 8, Redis, Spring AI (OpenAI), QueryDSL, OpenFeign
