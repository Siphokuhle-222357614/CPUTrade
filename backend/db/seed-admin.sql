-- Run this AFTER the backend has started at least once (so the `users`
-- table exists — Hibernate creates it automatically on boot).
--
-- Logs in as: username `admin`, password `Admin123!`
-- Change the password before this ever runs anywhere but localhost.
--
-- Safe to run more than once: INSERT IGNORE skips it if `admin` already exists.

USE cputrade;

INSERT IGNORE INTO users (username, email, campus_handle, password_hash, role, verified, vendor_approved, account_status, created_at)
VALUES (
  'admin',
  'admin@cputmarket.ac.za',
  'admin@cputmarket.ac.za',
  '$2b$10$l5OcihujLu8354gkqn9v4ehF/J5vH2VG/nR9PazmMc.pRsq1w/R76',
  'ADMIN',
  true,
  true,
  'ACTIVE', -- NOT NULL with no database default, so it must be set explicitly
  NOW()
);
