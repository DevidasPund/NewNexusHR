-- NexusHR — optional MySQL bootstrap.
-- The app auto-creates the schema (createDatabaseIfNotExist=true) using your
-- DB_USERNAME/DB_PASSWORD, so this file is OPTIONAL. Use it only if you prefer a
-- dedicated, least-privilege DB user instead of root.
--
-- Run with:  mysql -u root -p < src/main/resources/db/init.sql
-- Then start the API with:
--   DB_USERNAME=nexushr DB_PASSWORD=nexushr_pw ./mvnw spring-boot:run

CREATE DATABASE IF NOT EXISTS nexushr
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE USER IF NOT EXISTS 'nexushr'@'localhost' IDENTIFIED BY 'nexushr_pw';
GRANT ALL PRIVILEGES ON nexushr.* TO 'nexushr'@'localhost';
FLUSH PRIVILEGES;
