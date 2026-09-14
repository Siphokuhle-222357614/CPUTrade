-- Run this AFTER the backend has started at least once (so the `users`
-- table exists — Hibernate creates it automatically on boot).
--
-- Logs in as: username `admin`, password `Admin123!`
-- Change the password before this ever runs anywhere but localhost.

USE cputrade;

INSERT INTO users (username, email, campus_handle, password_hash, role, verified, vendor_approved, created_at)
VALUES (
  'admin',
  'admin@cputmarket.ac.za',
  'admin@cputmarket.ac.za',
  '$2b$10$l5OcihujLu8354gkqn9v4ehF/J5vH2VG/nR9PazmMc.pRsq1w/R76',
  'ADMIN',
  true,
  true,
  NOW()
);
