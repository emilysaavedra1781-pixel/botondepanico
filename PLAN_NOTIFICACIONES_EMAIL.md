# Plan de Notificaciones por Correo Electrónico

Este documento detalla la investigación y el plan para integrar notificaciones automáticas por correo electrónico al reportar emergencias.

## 1. Punto de Creación de Emergencias
La creación de emergencias se centraliza principalmente en `EmergenciaService.java`.

### Método: `registrarEmergencia`
```java
public void registrarEmergencia(Usuario usuario, String tipoEmergencia,
                                 String latitud, String longitud,
                                 String ubicacion, String fotoVideo) {
    // ... persistencia en DB ...
    // ... lógica de envío de correo ...
}
```
Este método ya realiza el guardado en la base de datos y contiene una lógica preliminar para el envío de correos.

## 2. Estado de EmailService.java
El servicio `EmailService.java` ya existe y tiene implementado el método `enviarAlerta`.
- **Funcionalidad:** Arma un mensaje simple con el tipo de emergencia, nombre del ciudadano, celular y un enlace a Google Maps basado en las coordenadas.
- **Estado:** Utiliza `JavaMailSender` de Spring Boot.

## 3. Configuración SMTP
Las credenciales y la configuración del servidor de correo ya están presentes:
- **application.properties:** Define el host (`smtp.gmail.com`), puerto (`587`), y hace referencia a variables de entorno para el usuario y la contraseña.
- **.env:** Contiene valores para `MAIL_USERNAME` y `MAIL_PASSWORD`.
- **Estado:** La infraestructura está lista para ser usada.

## 4. Conexión Actual
En `EmergenciaService.java`, el método `registrarEmergencia` **ya intenta llamar** a `emailService.enviarAlerta`. 
Sin embargo, el código está envuelto en un bloque `try-catch` que silencia cualquier error (`catch (Exception e) {}`), lo que podría estar ocultando fallos en el envío real.

## 5. Propuesta de Mejora y Conexión
Aunque la llamada existe, se proponen las siguientes mejoras:
- **Mejorar el contenido del correo:** Agregar el distrito y la dirección textual si están disponibles.
- **Manejo de errores:** Loguear el error en lugar de silenciarlo para diagnosticar por qué no llega el correo (posiblemente contraseña de aplicación de Gmail incorrecta).
- **Consistencia con UsuarioModuloService:** Verificar si el flujo de "Botón SOS" (activado desde `UsuarioModuloService.activarSos`) también debe disparar correos, ya que actualmente parece que solo lo hace `EmergenciaService.registrarEmergencia`.

## 6. Destinatarios (Categorías Actualizadas)
El sistema utiliza una lógica de derivación en `obtenerCorreo(String tipo)` basada en categorías oficiales:
- **SAMU:** alonsocisnerosilz@gmail.com
- **BOMBEROS:** camilabr0502@gmail.com
- **POLICIA:** emilysaacedra200417@gmail.com
- **Por defecto:** emilysaavedra.17.8.1@gmail.com

## 7. Cambios Realizados
1. **EmergenciaService.java:** Se actualizó el método `obtenerCorreo` para soportar el nuevo mapeo (`SAMU`, `BOMBEROS`, `POLICIA`).
2. **home.html:** Se modificaron las opciones del selector de tipo de emergencia para que coincidan con los nuevos valores del backend.
3. **home.js:** Se actualizó la lógica de reconocimiento de voz para que asigne automáticamente las nuevas categorías al detectar palabras clave.
4. **NotificacionService.java:** Se ajustó la recomendación de entidades y correos sugeridos para alinearse con la nueva estructura de categorías.

## 8. Análisis del Flujo del Operador (Diagnóstico)
Se ha investigado el registro de emergencias desde el lado del operador.

### Punto de Registro (Operador)
El registro se realiza en `OperadorService.java`.
- **Método:** `crearReporteTelefonico`
- **Controlador:** `OperadorController.crearReporteTelefonico` (POST `/operador/reporte-telefonico`).

### Estado de Notificación
El método `crearReporteTelefonico` **NO envía ningún correo electrónico** actualmente. Solo guarda la emergencia en la DB con estado `PENDIENTE` y origen `TELEFONICO`.

### Diferencia con el Flujo Ciudadano
- **Ciudadano (`EmergenciaService`):** El envío es **automático e inmediato**.
- **Operador (`OperadorService`):** El envío es **manual**. El operador primero registra la llamada y luego tiene una opción explícita para "Notificar Autoridad" (`NotificacionService.notificarAutoridad`).

### Recomendación de Negocio
- **¿Por qué es manual?** Probablemente para permitir que el operador filtre llamadas falsas antes de alertar a las autoridades reales.
- **Sugerencia:** Mantener el flujo manual para reportes telefónicos pero asegurar que el método `notificarAutoridad` en `NotificacionService` use la nueva lógica de correos unificada.

---
**Documentado por:** AI Assistant
**Fecha:** Junio 2024
