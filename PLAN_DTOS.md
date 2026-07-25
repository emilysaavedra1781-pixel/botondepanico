# Plan de Implementación de DTOs (Data Transfer Objects)

Este documento detalla la estrategia para mejorar la seguridad y eficiencia del proyecto mediante el uso de DTOs.

## 1. Clasificación de Riesgos y Prioridad

| Nivel | Descripción | Casos Detectados | Prioridad |
| :--- | :--- | :--- | :--- |
| **CRÍTICO** | Exposición de contraseñas o datos sensibles en sesión. | `Usuario`, `Operador`, `SuperAdmin` en `HttpSession`. | ALTA |
| **IMPORTANTE** | Ineficiencia por carga de binarios (videos/fotos) en listas. | `Emergencia` con su lista de `Evidencia` en dashboards. | MEDIA |
| **OPCIONAL** | Mejora de mantenimiento y desacoplamiento. | Formularios complejos y respuestas JSON. | BAJA |

---

## 2. Implementación Realizada

### Fase 1: Seguridad (COMPLETADO)
- **DTOs Creados**:
    - `AuthResponseDTO`: Contiene info básica del perfil (id, nombre, rol, distrito) sin contraseñas.
    - `RegistroUsuarioDTO`: Para validación robusta en el registro.
- **Cambios**:
    - `AuthController`, `UsuarioController`, `OperadorController` y `AdminController` ahora guardan/recuperan el DTO de la sesión.
    - Validación de registro centralizada.

### Fase 2: Eficiencia (COMPLETADO)
- **DTO Creado**:
    - `EmergenciaResumenDTO`: Objeto ligero para listados. Excluye la colección de evidencias (evita carga de bytes en memoria).
- **Cambios**:
    - `EmergenciaRepository`: Consultas optimizadas con `SELECT new botondepanico.dto.EmergenciaResumenDTO(...)`.
    - `OperadorService`, `MonitoreoService`: Métodos de listado refactorizados para devolver DTOs.
    - **Vistas Actualizadas**: Dashboards e Historiales (Admin/Operador) ajustados para usar los campos del DTO.

---

## 3. Próximos Pasos

### Fase 3: Mantenimiento (PENDIENTE)
- **Objetivo**: Estandarizar respuestas JSON y simplificar controladores.
- **DTOs a crear**:
    - `EvidenciaDTO`: Para respuestas AJAX al subir archivos.
    - `CamaraDTO`: Para la gestión de cámaras.

---
**Actualizado por:** AI Assistant
**Fecha:** Junio 2024
