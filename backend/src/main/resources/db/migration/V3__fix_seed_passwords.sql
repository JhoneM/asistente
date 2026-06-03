-- V3 Corrección de contraseñas del seed
-- Hash BCrypt 10 rounds de "demo1234" generado con Spring BCryptPasswordEncoder

UPDATE users
SET hashed_password = '$2a$10$qIlnO.sUYlzSHokQa2W3N.rWGGomsSaX01IOdOfp34ADqx9iKostK'
WHERE email IN ('demo@habitpet.com', 'maria@habitpet.com', 'lucas@habitpet.com');
