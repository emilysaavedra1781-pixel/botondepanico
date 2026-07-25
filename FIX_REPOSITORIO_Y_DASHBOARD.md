# Plan de Corrección: Repositorio y Vista Dashboard

Este documento detalla los cambios técnicos que se aplicarán para resolver el error de búsqueda en PostgreSQL y la discrepancia de campos en el dashboard del operador.

## 1. Módulo Emergencia (Error `lower(bytea)`)

### Diagnóstico
PostgreSQL reporta que no puede aplicar la función `lower()` sobre datos binarios (`bytea`). Aunque la columna ya es `TEXT`, Hibernate 6 está bindeando el **parámetro** de búsqueda como `VARBINARY`.

### Solución: CAST Robusto en JPQL
Se aplicará un casteo explícito tanto a la **columna** como al **parámetro** dentro de `EmergenciaRepository.java`.

**Código a Aplicar:**
```java
"WHERE (:texto is null or " +
"  lower(cast(e.tipoEmergencia as string)) like lower(concat('%', cast(:texto as string), '%')) or " +
"  lower(cast(e.direccion as string))      like lower(concat('%', cast(:texto as string), '%')) or " +
"  lower(cast(e.usuario.nombre as string)) like lower(concat('%', cast(:texto as string), '%')) or " +
"  lower(cast(e.usuario.apellido as string)) like lower(concat('%', cast(:texto as string), '%'))) and " +
"(:estado is null or e.estado = :estado) and " +
"(:prioridad is null or e.prioridad = :prioridad) and " +
"(:tipo is null or lower(cast(e.tipoEmergencia as string)) like lower(concat('%', cast(:tipo as string), '%'))) and " +
"(:distrito is null or lower(cast(e.distrito as string)) like lower(concat('%', cast(:distrito as string), '%'))) "
```

---

## 2. Módulo Dashboard (Error de Propiedades)

### Diagnóstico
La vista `dashboard.html` intenta acceder a `em.usuario.nombre` (propiedad de Entidad), pero el controlador ahora envía una lista de `EmergenciaResumenDTO`, donde la propiedad es `em.usuarioNombre`.

### Solución: Alineación de Campos
Se actualizará la plantilla `src/main/resources/templates/operador/dashboard.html` para usar los campos correctos del DTO.

**Cambios:**
- `em.usuario.nombre` -> `em.usuarioNombre`
- `em.usuario.apellido` -> `em.usuarioApellido`

---

## 3. Recomendación para Error de DNI (Lombok)

Antes de realizar cambios adicionales en el código del DTO, se debe descartar un problema de caché de compilación.

**Pasos Sugeridos:**
1. Cerrar el IDE.
2. Ejecutar en terminal: `./mvnw clean install -DskipTests`
3. Reiniciar el IDE e invalidar cachés si es necesario.

---
**Generado por:** AI Assistant
**Fecha:** Julio 2024
