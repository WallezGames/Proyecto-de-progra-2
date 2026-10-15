-- =====================================================================
-- Sistema de Gestion para Taller Mecanico Multiespecialidad
-- Script 00: usuario de MySQL que utiliza la aplicacion
-- Ejecutar UNA SOLA VEZ, conectado como root.
-- El usuario y la contrasena deben coincidir con config/conexion.properties
-- =====================================================================

CREATE USER IF NOT EXISTS 'taller_app'@'localhost' IDENTIFIED BY 'TallerG6_2026';
ALTER USER 'taller_app'@'localhost' IDENTIFIED BY 'TallerG6_2026';
GRANT ALL PRIVILEGES ON taller_mecanico.* TO 'taller_app'@'localhost';
FLUSH PRIVILEGES;
