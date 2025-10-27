-- 티켓팅 데이터베이스 초기화 스크립트
-- UTF-8 설정
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- 데이터베이스가 존재하지 않을 경우 생성
CREATE DATABASE IF NOT EXISTS ticketing
DEFAULT CHARACTER SET utf8mb4
DEFAULT COLLATE utf8mb4_unicode_ci;

USE ticketing;

-- 권한 설정
GRANT ALL PRIVILEGES ON ticketing.* TO 'codin'@'%';
FLUSH PRIVILEGES;

-- 기본 테이블 생성은 JPA가 자동으로 처리
