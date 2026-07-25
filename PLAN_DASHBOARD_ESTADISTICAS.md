# Plan de Implementación: Dashboard de Estadísticas del Operador

Este documento detalla la planificación para la creación de un panel de control estadístico (Dashboard) destinado a los operadores del sistema "Botón de Pánico".

## 1. Objetivo
Proveer al operador de una herramienta visual avanzada para la toma de decisiones, permitiendo identificar patrones de incidentes por zona, tipo, género del usuario y tendencias temporales, además de ofrecer un resumen ejecutivo del estado del sistema.

## 2. Estado Actual
### Existente en `EstadisticaService.java`:
- Métodos funcionales para: `porTipo`, `porDistrito`, `porHora` y `porOperador`.
- Lógica de mapeo de resultados de base de datos (`List<Object[]>`) a `Map<String, Long>`.

### Datos Disponibles en Entidades:
- **Zona:** Campo `distrito` en la entidad `Emergencia`.
- **Tipo:** Campo `tipoEmergencia` en la entidad `Emergencia`.
- **Fecha:** Campo `fecha` en la entidad `Emergencia` (usado para el gráfico por hora).

### Datos Faltantes:
- **Género:** No existe el campo en la entidad `Usuario`.

## 3. Exploración Completa de Datos Disponibles

Tras un análisis profundo del modelo de datos, se han identificado las siguientes capacidades adicionales para el dashboard:

### A. Entidad Emergencia (Nivel Operativo)
- **Estados:** PENDIENTE, EN_ATENCION, AUTORIDAD_NOTIFICADA, RESUELTA, CANCELADA, RECHAZADA. Permite medir el flujo de trabajo.
- **Prioridades:** ALTA, MEDIA, BAJA. Crucial para identificar zonas críticas.
- **Origen:** SOS (App) o TELEFONICO. Permite medir la adopción tecnológica.
- **Tiempos:** `fecha` (creación) y `fechaActualizacion`. En conjunto con `HistorialEmergencia`, permite calcular el **Tiempo Medio de Respuesta (SLA)**.

### B. Entidad Usuario (Demografía)
- **Ubicación:** Campo `distrito` (residencia). Permite cruzar procedencia del usuario vs lugar del incidente.
- **Tipo Cuenta:** Google, Teléfono, etc.

### C. Entidad Operador (Desempeño)
- **Métricas:** Cantidad de casos asignados y resueltos.
- **Estado:** En línea/Fuera de línea. Permite medir la carga de trabajo por operador activo.

### D. Entidad Evidencia (Validación)
- **Multimedios:** Tipos (FOTO, VIDEO, AUDIO) y cantidad por emergencia. Sirve como indicador de la riqueza de información de cada alerta.

### Clasificación de Indicadores:
- **Inmediatos:** Volumen por estado, prioridades, distritos con más incidentes, origen de alertas.
- **Complejos (Requieren lógica):** Tiempo promedio de respuesta (SLA), carga de trabajo por operador, tendencia de resolución por tipo de emergencia.
- **Futuros (Requieren campos nuevos):** Género (en plan), Edad, Satisfacción del usuario (post-emergencia).

### Recomendación Operativa (Top 5 KPIs):
1. **Estado de la Cola:** Visualización del embudo de atención en tiempo real.
2. **Tiempo Medio de Respuesta:** El KPI de calidad más importante para un centro de emergencias.
3. **Mapa de Calor por Prioridad ALTA:** Enfoque en los incidentes más graves.
4. **Carga de Trabajo por Operador:** Optimización del recurso humano.
5. **Tendencia Horaria:** Para planificación de turnos y guardias.

## 4. Consideraciones sobre el campo "Género"
- **Usuarios Existentes:** Los registros actuales tendrán este campo como `null`. En las estadísticas se agruparán bajo la etiqueta "No especificado".
- **Captura de Datos:** Se deberá actualizar el formulario de `registro-usuario.html` para incluir un campo de selección (Male, Female, Other/Prefer not to say).
- **Sensibilidad:** Se recomienda que sea **opcional** en el registro para evitar fricción con el ciudadano, pero se incentivará su llenado para fines estadísticos de seguridad ciudadana.

## 4. Plan de Implementación - Backend
### Cambios en `Usuario.java`:
- Agregar `private String genero;`.
- Actualizar anotaciones de Lombok.

### `EmergenciaRepository.java`:
- Crear consulta JPQL: `SELECT e.usuario.genero, COUNT(e) FROM Emergencia e GROUP BY e.usuario.genero`.
- Asegurar que las consultas existentes de distritos y tipos sean eficientes.

### `EstadisticaService.java`:
- Agregar método `porGenero()`.
- Optimizar el mapeo de datos para manejar valores nulos.

### `OperadorController.java`:
- Nuevo endpoint `@GetMapping("/operador/estadisticas")`.
- Cargar los mapas de datos en el `Model` de Thymeleaf.

## 5. Plan de Implementación - Frontend
### Estructura de `estadisticas.html`:
- **Cards Superiores:** Total Emergencias, Pendientes, En Atención, Resueltas.
- **Grilla de Gráficos (Dashboard):**
    - **Gráfico de Barras (Zonas):** Para comparar el volumen de incidentes entre distritos.
    - **Gráfico de Torta/Dona (Tipos):** Para ver la distribución por categoría (Robo, Incendio, Salud, etc.).
    - **Gráfico de Torta (Género):** Distribución demográfica de los reportantes.
    - **Gráfico de Líneas (Tendencia por Hora):** Para identificar picos de inseguridad durante el día.

### Integración de Chart.js:
- Inclusión de la librería vía CDN.
- Uso de `th:inline="javascript"` en Thymeleaf para inyectar los Mapas de Java como objetos JSON directamente en las configuraciones de los gráficos.

## 6. Orden de Pasos Sugerido
1. **Modelo:** Agregar campo `genero` a `Usuario` y actualizar DB.
2. **Repositorio:** Crear la consulta de agregación por género.
3. **Servicio:** Implementar el método en `EstadisticaService`.
4. **Controlador:** Crear la ruta y preparar el envío de datos.
5. **Vista:** Diseñar el esqueleto HTML con los contenedores para gráficos.
6. **Frontend JS:** Integrar Chart.js y renderizar los datos.
7. **Formularios:** Actualizar el registro para capturar el género en nuevos usuarios.

## 7. Riesgos o Dudas Pendientes
- **Privacidad:** ¿Existe alguna normativa local que impida recolectar el género del ciudadano sin un consentimiento explícito adicional?
- **Rendimiento:** Si la base de datos crece mucho, estas consultas de agregación podrían volverse lentas. Se considerará el uso de índices compuestos si es necesario.
- **Versión de Chart.js:** Se utilizará la v4.x por ser la más estable y moderna.

---
**Documentado por:** AI Assistant
**Fecha:** Junio 2024

## Fase 1: Indicadores Inmediatos - Seguimiento de Implementación

### Paso 1 - Backend (Repository): Completado
- **Archivos modificados:** `EmergenciaRepository.java`.
- **Nuevos métodos:**
    - `contarPorEstado()`: Agregación nativa por el campo `estado`.
    - `contarPorPrioridad()`: Agregación nativa por el campo `prioridad`.
    - `contarPorOrigen()`: Agregación nativa por el campo `origen`.
- **Decisión:** Se mantuvo `countByEstado(enum)` por separado para las consultas individuales de las cards, mientras que los métodos `contarPor...` se usan para alimentar los gráficos de Chart.js.
- **Estado:** Compilación verificada (métodos agregados a la interfaz).

### Paso 2 - Backend (Service): Completado
- **Archivos modificados:** `EstadisticaService.java`.
- **Nuevos métodos:**
    - `porEstado()`, `porPrioridad()`, `porOrigen()`: Transforman los datos brutos del repositorio en mapas `Map<String, Long>` listos para el frontend.
    - `resumenGeneral()`: Consolida los conteos de Total, Pendientes, En Atención y Resueltas para las tarjetas de resumen.
- **Estado:** Compilación verificada. Los métodos están listos para ser consumidos por el controlador.

### Paso 3 - Backend (Controller): Completado
- **Archivos modificados:** `OperadorController.java`.
- **Nuevo endpoint:** `/operador/estadisticas`.
- **Lógica:** Inyección de `EstadisticaService` en el constructor y preparación del `Model` con los datos de Resumen, Estado, Prioridad, Origen y Distrito.
- **Estado:** Compilación verificada.

### Paso 4 - Frontend (Vista): Completado
- **Archivo creado:** `src/main/resources/templates/operador/estadisticas.html`.
- **Componentes:**
    - 4 Tarjetas de resumen (Total, Pendientes, Atencion, Resueltas).
    - 4 Gráficos usando **Chart.js**: Barras (Estado), Dona (Prioridad), Barras Horizontales (Distrito) y Torta (Origen).
- **Integración:** Uso de `th:inline="javascript"` para inyectar los mapas de Java como objetos JSON directamente en los scripts de Chart.js.
- **Estado:** Estructura y lógica de visualización finalizada.

### Paso 5 - Ruta/Menú: Completado
- **Archivo modificado:** `src/main/resources/templates/operador/fragments.html`.
- **Cambio:** Se agregó el enlace "Estadísticas y Reportes" al sidebar del operador, con el ícono `bi-graph-up`.
- **Estado:** Navegación integrada correctamente.

---
## Conclusión de Fase 1
Se han implementado satisfactoriamente todos los indicadores inmediatos disponibles en la base de datos. El operador ahora cuenta con una herramienta visual para monitorear el estado del sistema en tiempo real.

**Siguiente Fase:** Fase 2 - Demografía (Género) y Tiempos de Respuesta (SLA).
