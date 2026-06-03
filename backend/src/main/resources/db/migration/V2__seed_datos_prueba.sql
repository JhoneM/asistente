-- V2 Datos de prueba HabitPet
-- Password "demo1234" con BCrypt 10 rounds

INSERT INTO users (id, email, hashed_password, display_name, timezone) VALUES
('user-demo-001',  'demo@habitpet.com',  '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Demo User',    'America/Argentina/Buenos_Aires'),
('user-maria-002', 'maria@habitpet.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'María García', 'America/Argentina/Buenos_Aires'),
('user-lucas-003', 'lucas@habitpet.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Lucas Pérez',  'UTC');

INSERT INTO pets (id, user_id, pet_name, pet_type, state, xp, level) VALUES
('pet-demo-001',  'user-demo-001',  'Chispa', 'CAT',    'EXCELLENT', 820, 5),
('pet-maria-002', 'user-maria-002', 'Nube',   'DOG',    'NEUTRAL',   310, 3),
('pet-lucas-003', 'user-lucas-003', 'Dragón', 'DRAGON', 'POOR',      120, 1);

INSERT INTO habits (id, user_id, name, category, weekly_frequency, description) VALUES
('habit-demo-001', 'user-demo-001', 'Tomar 2L de agua',   'HEALTH',    7, 'Hidratación diaria'),
('habit-demo-002', 'user-demo-001', 'Estudiar 1 hora',    'STUDY',     5, 'Dedicar tiempo al estudio'),
('habit-demo-003', 'user-demo-001', 'Salir a correr',     'SPORT',     3, 'Cardio al aire libre'),
('habit-demo-004', 'user-demo-001', 'Meditar 10 min',     'WELLNESS',  7, 'Meditación matutina'),
('habit-demo-005', 'user-demo-001', 'Desayuno saludable', 'NUTRITION', 5, 'Sin azúcar procesada');

INSERT INTO habits (id, user_id, name, category, weekly_frequency, description) VALUES
('habit-maria-001', 'user-maria-002', 'Yoga matutino',        'WELLNESS',  3, 'Yoga al despertar'),
('habit-maria-002', 'user-maria-002', 'Leer 20 páginas',      'STUDY',     5, 'Lectura diaria'),
('habit-maria-003', 'user-maria-002', 'Sin azúcar procesada', 'NUTRITION', 7, 'Dieta sin procesados'),
('habit-maria-004', 'user-maria-002', 'Caminar 30 min',       'SPORT',     4, 'Caminata vespertina');

INSERT INTO habits (id, user_id, name, category, weekly_frequency, description) VALUES
('habit-lucas-001', 'user-lucas-003', 'Ir al gimnasio',        'SPORT',  3, 'Entrenamiento de fuerza'),
('habit-lucas-002', 'user-lucas-003', 'Dormir 8 horas',        'HEALTH', 7, 'Descanso completo'),
('habit-lucas-003', 'user-lucas-003', 'Estudiar para parcial', 'STUDY',  5, 'Preparación de exámenes');

-- Demo: cumplimiento casi perfecto (14 días)
INSERT INTO completion_records (id, habit_id, user_id, date) VALUES
('cr-d001-00','habit-demo-001','user-demo-001', CURRENT_DATE - INTERVAL '0 days'),
('cr-d001-01','habit-demo-001','user-demo-001', CURRENT_DATE - INTERVAL '1 days'),
('cr-d001-02','habit-demo-001','user-demo-001', CURRENT_DATE - INTERVAL '2 days'),
('cr-d001-03','habit-demo-001','user-demo-001', CURRENT_DATE - INTERVAL '3 days'),
('cr-d001-04','habit-demo-001','user-demo-001', CURRENT_DATE - INTERVAL '4 days'),
('cr-d001-05','habit-demo-001','user-demo-001', CURRENT_DATE - INTERVAL '5 days'),
('cr-d001-06','habit-demo-001','user-demo-001', CURRENT_DATE - INTERVAL '6 days'),
('cr-d001-07','habit-demo-001','user-demo-001', CURRENT_DATE - INTERVAL '7 days'),
('cr-d001-08','habit-demo-001','user-demo-001', CURRENT_DATE - INTERVAL '8 days'),
('cr-d001-09','habit-demo-001','user-demo-001', CURRENT_DATE - INTERVAL '9 days'),
('cr-d001-10','habit-demo-001','user-demo-001', CURRENT_DATE - INTERVAL '10 days'),
('cr-d001-11','habit-demo-001','user-demo-001', CURRENT_DATE - INTERVAL '11 days'),
('cr-d001-12','habit-demo-001','user-demo-001', CURRENT_DATE - INTERVAL '12 days'),
('cr-d001-13','habit-demo-001','user-demo-001', CURRENT_DATE - INTERVAL '13 days'),
('cr-d002-00','habit-demo-002','user-demo-001', CURRENT_DATE - INTERVAL '0 days'),
('cr-d002-01','habit-demo-002','user-demo-001', CURRENT_DATE - INTERVAL '1 days'),
('cr-d002-02','habit-demo-002','user-demo-001', CURRENT_DATE - INTERVAL '2 days'),
('cr-d002-03','habit-demo-002','user-demo-001', CURRENT_DATE - INTERVAL '3 days'),
('cr-d002-04','habit-demo-002','user-demo-001', CURRENT_DATE - INTERVAL '4 days'),
('cr-d002-07','habit-demo-002','user-demo-001', CURRENT_DATE - INTERVAL '7 days'),
('cr-d002-08','habit-demo-002','user-demo-001', CURRENT_DATE - INTERVAL '8 days'),
('cr-d002-09','habit-demo-002','user-demo-001', CURRENT_DATE - INTERVAL '9 days'),
('cr-d002-10','habit-demo-002','user-demo-001', CURRENT_DATE - INTERVAL '10 days'),
('cr-d002-11','habit-demo-002','user-demo-001', CURRENT_DATE - INTERVAL '11 days'),
('cr-d003-00','habit-demo-003','user-demo-001', CURRENT_DATE - INTERVAL '0 days'),
('cr-d003-02','habit-demo-003','user-demo-001', CURRENT_DATE - INTERVAL '2 days'),
('cr-d003-04','habit-demo-003','user-demo-001', CURRENT_DATE - INTERVAL '4 days'),
('cr-d003-07','habit-demo-003','user-demo-001', CURRENT_DATE - INTERVAL '7 days'),
('cr-d003-09','habit-demo-003','user-demo-001', CURRENT_DATE - INTERVAL '9 days'),
('cr-d003-11','habit-demo-003','user-demo-001', CURRENT_DATE - INTERVAL '11 days'),
('cr-d004-00','habit-demo-004','user-demo-001', CURRENT_DATE - INTERVAL '0 days'),
('cr-d004-01','habit-demo-004','user-demo-001', CURRENT_DATE - INTERVAL '1 days'),
('cr-d004-02','habit-demo-004','user-demo-001', CURRENT_DATE - INTERVAL '2 days'),
('cr-d004-03','habit-demo-004','user-demo-001', CURRENT_DATE - INTERVAL '3 days'),
('cr-d004-04','habit-demo-004','user-demo-001', CURRENT_DATE - INTERVAL '4 days'),
('cr-d004-05','habit-demo-004','user-demo-001', CURRENT_DATE - INTERVAL '5 days'),
('cr-d004-07','habit-demo-004','user-demo-001', CURRENT_DATE - INTERVAL '7 days'),
('cr-d004-08','habit-demo-004','user-demo-001', CURRENT_DATE - INTERVAL '8 days'),
('cr-d004-09','habit-demo-004','user-demo-001', CURRENT_DATE - INTERVAL '9 days'),
('cr-d004-10','habit-demo-004','user-demo-001', CURRENT_DATE - INTERVAL '10 days'),
('cr-d004-11','habit-demo-004','user-demo-001', CURRENT_DATE - INTERVAL '11 days'),
('cr-d004-12','habit-demo-004','user-demo-001', CURRENT_DATE - INTERVAL '12 days'),
('cr-d004-13','habit-demo-004','user-demo-001', CURRENT_DATE - INTERVAL '13 days'),
('cr-d005-00','habit-demo-005','user-demo-001', CURRENT_DATE - INTERVAL '0 days'),
('cr-d005-01','habit-demo-005','user-demo-001', CURRENT_DATE - INTERVAL '1 days'),
('cr-d005-02','habit-demo-005','user-demo-001', CURRENT_DATE - INTERVAL '2 days'),
('cr-d005-03','habit-demo-005','user-demo-001', CURRENT_DATE - INTERVAL '3 days'),
('cr-d005-04','habit-demo-005','user-demo-001', CURRENT_DATE - INTERVAL '4 days'),
('cr-d005-07','habit-demo-005','user-demo-001', CURRENT_DATE - INTERVAL '7 days'),
('cr-d005-08','habit-demo-005','user-demo-001', CURRENT_DATE - INTERVAL '8 days'),
('cr-d005-09','habit-demo-005','user-demo-001', CURRENT_DATE - INTERVAL '9 days'),
('cr-d005-10','habit-demo-005','user-demo-001', CURRENT_DATE - INTERVAL '10 days'),
('cr-d005-11','habit-demo-005','user-demo-001', CURRENT_DATE - INTERVAL '11 days');

-- María: irregular (buena semana pasada, mala esta semana)
INSERT INTO completion_records (id, habit_id, user_id, date) VALUES
('cr-m001-07','habit-maria-001','user-maria-002', CURRENT_DATE - INTERVAL '7 days'),
('cr-m001-09','habit-maria-001','user-maria-002', CURRENT_DATE - INTERVAL '9 days'),
('cr-m001-11','habit-maria-001','user-maria-002', CURRENT_DATE - INTERVAL '11 days'),
('cr-m001-13','habit-maria-001','user-maria-002', CURRENT_DATE - INTERVAL '13 days'),
('cr-m001-02','habit-maria-001','user-maria-002', CURRENT_DATE - INTERVAL '2 days'),
('cr-m002-07','habit-maria-002','user-maria-002', CURRENT_DATE - INTERVAL '7 days'),
('cr-m002-08','habit-maria-002','user-maria-002', CURRENT_DATE - INTERVAL '8 days'),
('cr-m002-09','habit-maria-002','user-maria-002', CURRENT_DATE - INTERVAL '9 days'),
('cr-m002-10','habit-maria-002','user-maria-002', CURRENT_DATE - INTERVAL '10 days'),
('cr-m002-13','habit-maria-002','user-maria-002', CURRENT_DATE - INTERVAL '13 days'),
('cr-m002-01','habit-maria-002','user-maria-002', CURRENT_DATE - INTERVAL '1 days'),
('cr-m002-04','habit-maria-002','user-maria-002', CURRENT_DATE - INTERVAL '4 days'),
('cr-m003-07','habit-maria-003','user-maria-002', CURRENT_DATE - INTERVAL '7 days'),
('cr-m003-08','habit-maria-003','user-maria-002', CURRENT_DATE - INTERVAL '8 days'),
('cr-m003-09','habit-maria-003','user-maria-002', CURRENT_DATE - INTERVAL '9 days'),
('cr-m003-10','habit-maria-003','user-maria-002', CURRENT_DATE - INTERVAL '10 days'),
('cr-m003-11','habit-maria-003','user-maria-002', CURRENT_DATE - INTERVAL '11 days'),
('cr-m003-12','habit-maria-003','user-maria-002', CURRENT_DATE - INTERVAL '12 days'),
('cr-m003-13','habit-maria-003','user-maria-002', CURRENT_DATE - INTERVAL '13 days'),
('cr-m003-00','habit-maria-003','user-maria-002', CURRENT_DATE - INTERVAL '0 days'),
('cr-m003-02','habit-maria-003','user-maria-002', CURRENT_DATE - INTERVAL '2 days'),
('cr-m004-07','habit-maria-004','user-maria-002', CURRENT_DATE - INTERVAL '7 days'),
('cr-m004-08','habit-maria-004','user-maria-002', CURRENT_DATE - INTERVAL '8 days'),
('cr-m004-10','habit-maria-004','user-maria-002', CURRENT_DATE - INTERVAL '10 days'),
('cr-m004-12','habit-maria-004','user-maria-002', CURRENT_DATE - INTERVAL '12 days'),
('cr-m004-03','habit-maria-004','user-maria-002', CURRENT_DATE - INTERVAL '3 days');

-- Lucas: muy pocos check-ins, casi nada reciente
INSERT INTO completion_records (id, habit_id, user_id, date) VALUES
('cr-l001-10','habit-lucas-001','user-lucas-003', CURRENT_DATE - INTERVAL '10 days'),
('cr-l001-13','habit-lucas-001','user-lucas-003', CURRENT_DATE - INTERVAL '13 days'),
('cr-l002-10','habit-lucas-002','user-lucas-003', CURRENT_DATE - INTERVAL '10 days'),
('cr-l002-11','habit-lucas-002','user-lucas-003', CURRENT_DATE - INTERVAL '11 days'),
('cr-l002-12','habit-lucas-002','user-lucas-003', CURRENT_DATE - INTERVAL '12 days'),
('cr-l002-13','habit-lucas-002','user-lucas-003', CURRENT_DATE - INTERVAL '13 days'),
('cr-l003-08','habit-lucas-003','user-lucas-003', CURRENT_DATE - INTERVAL '8 days'),
('cr-l003-11','habit-lucas-003','user-lucas-003', CURRENT_DATE - INTERVAL '11 days');

INSERT INTO notifications (id, user_id, type, message, is_read) VALUES
('notif-001', 'user-lucas-003', 'PET_CRITICAL',     '¡Tu mascota está en peligro! ¡No la abandones!', FALSE),
('notif-002', 'user-demo-001',  'PET_LEVEL_UP',     '¡Chispa subió al nivel 5! ¡Excelente trabajo!',  TRUE),
('notif-003', 'user-maria-002', 'STREAK_MILESTONE', '¡Completaste 7 días seguidos de lectura!',        TRUE);
