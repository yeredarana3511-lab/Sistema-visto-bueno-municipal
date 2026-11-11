-- ====================================================================================
-- Script de Migración de Roles, Permisos Operativos y BCrypt para MariaDB
-- Proyecto: Sistema Visto Bueno Municipal
-- Base de datos: sistema_visto_bueno
-- Segregación Estricta de Funciones (Principio de Mínimo Privilegio)
-- ====================================================================================

USE `sistema_visto_bueno`;

-- 1. Actualización o Inserción de Roles con su Matriz de Permisos JSON:
-- ------------------------------------------------------------------------------------
-- A) SUPER_ADMIN (Administrador): Exclusivamente Mantenimiento Técnico, Usuarios y Roles
--    * NOTA: El Administrador NO puede modificar solicitudes, ni procesar OCR, ni evaluar vistos buenos.
INSERT INTO `roles` (`id`, `clave`, `nombre`, `permisos_json`) 
VALUES (1, 'SUPER_ADMIN', 'Super Administrador', 
'["USUARIOS:GESTIONAR","ROLES:GESTIONAR","SISTEMA:MANTENIMIENTO","SEGUIMIENTO:CONSULTAR"]')
ON DUPLICATE KEY UPDATE 
    `clave` = 'SUPER_ADMIN',
    `nombre` = 'Super Administrador',
    `permisos_json` = '["USUARIOS:GESTIONAR","ROLES:GESTIONAR","SISTEMA:MANTENIMIENTO","SEGUIMIENTO:CONSULTAR"]';

-- B) SECRETARIA (Secretari@ / OCR): Ella hace todos los procesos operativos
--    * Subir solicitud, procesar con OCR, revisar/corregir datos y enviar solicitud.
INSERT INTO `roles` (`id`, `clave`, `nombre`, `permisos_json`) 
VALUES (2, 'SECRETARIA', 'Secretari@', 
'["SOLICITUDES:CONSULTAR","SOLICITUDES:CREAR","SOLICITUDES:EDITAR","SOLICITUDES:CONFIRMAR_OCR","SOLICITUDES:ENVIAR","SEGUIMIENTO:CONSULTAR"]')
ON DUPLICATE KEY UPDATE 
    `clave` = 'SECRETARIA',
    `nombre` = 'Secretari@',
    `permisos_json` = '["SOLICITUDES:CONSULTAR","SOLICITUDES:CREAR","SOLICITUDES:EDITAR","SOLICITUDES:CONFIRMAR_OCR","SOLICITUDES:ENVIAR","SEGUIMIENTO:CONSULTAR"]';

-- C) SUPERVISOR: Reducido a dar el Visto Bueno (Aprobar o Rechazar)
--    * No captura solicitudes ni administra usuarios.
INSERT INTO `roles` (`id`, `clave`, `nombre`, `permisos_json`) 
VALUES (3, 'SUPERVISOR', 'Supervisor', 
'["SOLICITUDES:CONSULTAR","VISTO_BUENO:APROBAR_RECHAZAR","OFICIOS:CONSULTAR","OFICIOS:GENERAR","SEGUIMIENTO:CONSULTAR"]')
ON DUPLICATE KEY UPDATE 
    `clave` = 'SUPERVISOR',
    `nombre` = 'Supervisor',
    `permisos_json` = '["SOLICITUDES:CONSULTAR","VISTO_BUENO:APROBAR_RECHAZAR","OFICIOS:CONSULTAR","OFICIOS:GENERAR","SEGUIMIENTO:CONSULTAR"]';

-- Actualizar por si existían roles con los nombres anteriores:
UPDATE `roles` SET `clave` = 'SECRETARIA', `nombre` = 'Secretari@' WHERE `clave` IN ('CAJERO', 'CAJ');
UPDATE `roles` SET `clave` = 'SUPERVISOR', `nombre` = 'Supervisor' WHERE `clave` IN ('DIRECTOR', 'DIR');


-- 2. Actualización de Contraseñas Seguras con BCrypt (Coste 10):
-- ------------------------------------------------------------------------------------
-- A) Usuario Administrador: ADMIN01 (Contraseña: admin123)
UPDATE `usuarios`
SET `password_hash` = '$2a$10$gkm.Efi.QZMNwlN0ImcHbeXDikE47ifbg7lY5WnIkfHTwD0jMb9li',
    `rol_id` = (SELECT id FROM `roles` WHERE `clave` = 'SUPER_ADMIN' LIMIT 1)
WHERE `numero_empleado` = 'ADMIN01';

-- B) Usuario Secretari@: CAJ01 o SEC01 (Contraseña: secretaria123)
UPDATE `usuarios`
SET `password_hash` = '$2a$10$LWPQFd4TgC0grkM00ixWduxomrm0Ym1tnUB5fGZC1iyx9iVmgzFxu',
    `rol_id` = (SELECT id FROM `roles` WHERE `clave` = 'SECRETARIA' LIMIT 1)
WHERE `numero_empleado` IN ('CAJ01', 'SEC01');

-- C) Usuario Supervisor: SUP01 (Contraseña: supervisor123)
UPDATE `usuarios`
SET `password_hash` = '$2a$10$vVd66YLT.vn3OuUa7jWMM.xzs.VKf.yqaTuglAXxqdfqgxgUWvKuS',
    `rol_id` = (SELECT id FROM `roles` WHERE `clave` = 'SUPERVISOR' LIMIT 1)
WHERE `numero_empleado` = 'SUP01';


-- 3. Verificación de Roles, Permisos Asignados y Usuarios:
-- ------------------------------------------------------------------------------------
SELECT id, clave, nombre, permisos_json FROM `roles`;

SELECT 
    u.id, 
    u.numero_empleado, 
    u.nombre, 
    u.apellidos, 
    u.correo, 
    r.nombre AS rol, 
    u.activo 
FROM `usuarios` u
LEFT JOIN `roles` r ON u.rol_id = r.id;
