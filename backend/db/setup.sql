-- CPUTrade — one-time local database setup.
-- Run this against MySQL as an admin user (e.g. root). See backend/README.md
-- for how to run it and what to do next.

CREATE DATABASE IF NOT EXISTS cputrade
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

-- Change this password if you want something else — just update
-- spring.datasource.password in backend/src/main/resources/application.properties
-- (or set the SPRING_DATASOURCE_PASSWORD environment variable) to match.
CREATE USER IF NOT EXISTS 'cputrade_app'@'localhost' IDENTIFIED BY 'ChangeMe_StrongPass!';

GRANT ALL PRIVILEGES ON cputrade.* TO 'cputrade_app'@'localhost';
FLUSH PRIVILEGES;
