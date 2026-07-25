# Análisis del Proyecto - Botón de Pánico

Este documento detalla el diagnóstico técnico realizado sobre la arquitectura, configuración y problemas de rendimiento detectados en el sistema.

## 1. Validación de Entorno (Spring Boot)
- **Estado:** Error crítico detectado.
- **Hallazgo:** El archivo `pom.xml` referencia la versión `3.5.14` de Spring Boot, la cual no existe en los repositorios oficiales.
- **Impacto:** Fallos en la descarga de dependencias y posibles incompatibilidades de runtime.
- **Recomendación:** Corregir a la versión **3.4.3** (estable), que garantiza soporte para Java 21 y Spring Security 6.

## 2. Integridad Referencial (Emergencia - Operador)
- **Configuración Actual:** Se utiliza `ConstraintMode.NO_CONSTRAINT`.
- **Riesgo:** La base de datos no garantiza que el `operador_id` asignado a una emergencia sea válido. Esto puede generar "datos huérfanos" y errores de puntero nulo en la lógica de negocio al intentar acceder a datos del operador.
- **Recomendación:** Restaurar la restricción de llave foránea en la base de datos. La validación en la capa de servicio es insuficiente para garantizar la consistencia a largo plazo.

## 3. Arquitectura de Datos (DTOs)
- **Estado:** Ausentes.
- **Observación:** Las entidades JPA se exponen directamente desde los controladores a las vistas (Thymeleaf).
- **Riesgo:**
    - Exposición involuntaria de campos sensibles (passwords, fechas de auditoría).
    - Ineficiencia en la transferencia de datos (carga de objetos completos cuando solo se requieren 2 o 3 campos).
- **Recomendación:** Implementar DTOs específicos para las operaciones de:
    - Registro de emergencia.
    - Visualización en el dashboard del operador.
    - Reportes estadísticos.

## 4. Diagnóstico de Rendimiento (Subida de Video)
Se ha identificado que la lentitud en la subida de videos no se debe a un único bucle, sino a la arquitectura de persistencia:

### Causas Principales:
1. **Persistencia Binaria Inadecuada:** Los videos se almacenan como `bytea` (bloques de bytes) directamente en la tabla `evidencias`.
2. **Efecto Cascada en Memoria:** Al recuperar una `Emergencia` para asociarle un video nuevo, el uso de `@Data` (Lombok) puede disparar la carga de la colección `evidencias`. Si la emergencia ya tiene videos previos, Hibernate carga los bytes de todos esos videos en la RAM, colapsando el rendimiento.
3. **Consultas de "Recorrido" en Memoria:** Métodos como `obtenerUltimaEmergencia` recuperan listas completas de la base de datos para luego filtrarlas con `.stream().findFirst()`, en lugar de usar `LIMIT 1` a nivel SQL.
4. **Escaneo Secuencial:** La falta de índices en la columna `emergencia_id` de la tabla `evidencias` hace que el conteo de archivos (`countByEmergenciaId`) sea más lento a medida que crece la base de datos.

### Acciones Sugeridas:
- Mover el almacenamiento de videos fuera de la tabla principal o usar `FetchType.LAZY` real con proxies para el contenido binario.
- Optimizar las consultas en los Repositorios para usar `findFirst...` nativo.
- Excluir colecciones pesadas de los métodos `equals` y `hashCode` de las entidades.

---
**Analizado por:** AI Assistant
**Fecha:** Junio 2024
