-- 01-create-dbs.sql
CREATE DATABASE IF NOT EXISTS codin_lecture
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS ticketing
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

-- 유저가 이미 만들어져 있어도 안전하게 처리 (MySQL 8에서 ok)
CREATE USER IF NOT EXISTS 'codin'@'%';

-- 두 DB 모두 권한 부여
GRANT ALL PRIVILEGES ON codin_lecture.* TO 'codin'@'%';
GRANT ALL PRIVILEGES ON ticketing.*   TO 'codin'@'%';

FLUSH PRIVILEGES;