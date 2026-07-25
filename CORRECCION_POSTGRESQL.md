# Corrección Técnica de Base de Datos - PostgreSQL / Supabase

Este documento explica la causa técnica del error `function lower(bytea) does not exist` y proporciona el script de remediación para la base de datos en producción.

## 1. Diagnóstico Técnico
Tras la actualización a Spring Boot 3.4.x (que utiliza Hibernate 6), ciertas columnas de tipo `String` en Java fueron mapeadas automáticamente por Hibernate como objetos binarios (`bytea`) en PostgreSQL en lugar de cadenas de texto (`TEXT` o `VARCHAR`).

Esto ocurrió debido a que Hibernate 6 interpreta campos `String` sin longitud definida como potenciales "Large Objects". Como resultado, funciones de búsqueda como `LOWER()` y `LIKE` fallaban con un error 500, ya que PostgreSQL no puede aplicar funciones de texto sobre datos binarios.

## 2. Columnas Afectadas
- **Tabla `emergencias`:** `tipo_emergencia`, `direccion`.
- **Tabla `usuarios`:** `nombre`, `apellido`.

## 3. Garantía de Integridad de Datos
La conversión propuesta utiliza `convert_from(..., 'UTF8')`. Este método asegura que los bytes almacenados se interpreten correctamente como caracteres de texto sin pérdida de información y sin alterar los registros existentes.

---

## 4. Script de Remediación (Ejecutar en Supabase)

Por favor, copia y ejecuta el siguiente bloque de código en el **SQL Editor** de Supabase para corregir la estructura de las tablas:

```sql
-- ============================================================
-- SCRIPT DE CONVERSIÓN DE COLUMNAS BINARIAS A TEXTO (UTF-8)
-- ============================================================

-- 1. Corrección de la tabla 'emergencias'
ALTER TABLE emergencias 
  ALTER COLUMN tipo_emergencia TYPE TEXT USING convert_from(tipo_emergencia, 'UTF8'),
  ALTER COLUMN direccion TYPE TEXT USING convert_from(direccion, 'UTF8');

-- 2. Corrección de la tabla 'usuarios'
ALTER TABLE usuarios 
  ALTER COLUMN nombre TYPE TEXT USING convert_from(nombre, 'UTF8'),
  ALTER COLUMN apellido TYPE TEXT USING convert_from(apellido, 'UTF8');

-- 3. Verificación (Opcional: agrega 'distrito' si el error persiste)
-- ALTER TABLE emergencias ALTER COLUMN distrito TYPE TEXT USING convert_from(distrito, 'UTF8');
```

---

## 5. Cambios en Código Aplicados
Para evitar que este problema se repita al crear nuevas tablas, se ha añadido la anotación `columnDefinition = "TEXT"` en las entidades JPA:

- `Emergencia.java`: Actualizados `tipoEmergencia` y `direccion`.
- `Usuario.java`: Actualizados `nombre` y `apellido`.

---
**Documentado por:** AI Assistant
**Fecha:** Julio 2024
