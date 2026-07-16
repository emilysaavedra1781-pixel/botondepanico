package botondepanico.controller;

import botondepanico.model.*;
import botondepanico.service.CamaraService;
import botondepanico.service.EvidenciaService;
import botondepanico.service.NotificacionService;
import botondepanico.service.OperadorService;
import botondepanico.service.UbicacionService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Controller
@Validated
public class OperadorController {

    private final OperadorService operadorService;
    private final EvidenciaService evidenciaService;
    private final UbicacionService ubicacionService;
    private final NotificacionService notificacionService;
    private final CamaraService camaraService;

    public OperadorController(OperadorService operadorService,
                              EvidenciaService evidenciaService,
                              UbicacionService ubicacionService,
                              NotificacionService notificacionService,
                              CamaraService camaraService) {
        this.operadorService = operadorService;
        this.evidenciaService = evidenciaService;
        this.ubicacionService = ubicacionService;
        this.notificacionService = notificacionService;
        this.camaraService = camaraService;
    }

    @GetMapping("/operador/dashboard")
    public String dashboard(HttpSession session, Model model) {
        Operador operador = operadorAutenticado(session);
        if (operador == null) return "redirect:/login";
        base(model, operador, "dashboard");
        model.addAttribute("pendientes", operadorService.contarPendientes());
        model.addAttribute("enAtencion", operadorService.contarEnAtencion());
        model.addAttribute("atendidasHoy", operadorService.contarAtendidasHoy(operador));
        model.addAttribute("totalEmergencias", operadorService.contarTotal());
        model.addAttribute("emergenciasPendientes", operadorService.pendientes());
        model.addAttribute("emergenciasActivas", operadorService.activas());
        return "operador/dashboard";
    }

    @GetMapping("/operador/emergencias")
    public String emergencias(@RequestParam(required = false) String q,
                              @RequestParam(required = false) EstadoEmergencia estado,
                              @RequestParam(required = false) PrioridadEmergencia prioridad,
                              @RequestParam(required = false) String tipo,
                              @RequestParam(required = false) String distrito,
                              HttpSession session,
                              Model model) {
        Operador operador = operadorAutenticado(session);
        if (operador == null) return "redirect:/login";
        base(model, operador, "emergencias");
        model.addAttribute("emergencias", operadorService.buscar(q, estado, prioridad, tipo, distrito));
        model.addAttribute("estados", List.of(EstadoEmergencia.PENDIENTE, EstadoEmergencia.EN_ATENCION, EstadoEmergencia.AUTORIDAD_NOTIFICADA, EstadoEmergencia.RESUELTA, EstadoEmergencia.RECHAZADA));
        model.addAttribute("prioridades", PrioridadEmergencia.values());
        model.addAttribute("tipos", TipoEmergencia.values());
        return "operador/emergencias";
    }

    @GetMapping("/operador/reporte-telefonico")
    public String reporteTelefonicoForm(HttpSession session, Model model) {
        Operador operador = operadorAutenticado(session);
        if (operador == null) return "redirect:/login";
        base(model, operador, "emergencias");
        model.addAttribute("tipos", TipoEmergencia.values());
        model.addAttribute("prioridades", PrioridadEmergencia.values());
        return "operador/reporte-telefonico";
    }

    @PostMapping("/operador/reporte-telefonico")
    public String crearReporteTelefonico(@RequestParam @NotBlank(message = "El nombre es obligatorio") String nombre,
                                         @RequestParam(required = false) String apellido,
                                         @RequestParam @NotBlank(message = "El celular es obligatorio") @Pattern(regexp = "\\d{9}", message = "El celular debe tener 9 dígitos") String celular,
                                         @RequestParam(required = false) @Pattern(regexp = "^$|^\\d{8}$", message = "El DNI debe tener 8 dígitos") String dni,
                                         @RequestParam(required = false) String distrito,
                                         @RequestParam(required = false) String direccion,
                                         @RequestParam(required = false) String tipoEmergencia,
                                         @RequestParam(defaultValue = "MEDIA") String prioridad,
                                         @RequestParam(required = false) String descripcion,
                                         @RequestParam(value = "video", required = false) MultipartFile video,
                                         HttpSession session,
                                         RedirectAttributes redirectAttributes) throws IOException {
        Operador operador = operadorAutenticado(session);
        if (operador == null) return "redirect:/login";
        Emergencia emergencia = operadorService.crearReporteTelefonico(
            operador,
            nombre,
            apellido,
            celular,
            dni,
            distrito,
            direccion,
            tipoEmergencia,
            prioridad,
            descripcion
        );

        if (video != null && !video.isEmpty()) {
            guardarVideoEvidencia(video, emergencia);
        }

        redirectAttributes.addFlashAttribute("exito", "Reporte telefónico registrado correctamente.");
        return "redirect:/operador/emergencia/" + emergencia.getId() + "/clasificar";
    }

    @GetMapping("/operador/emergencia/{id}")
    public String detalle(@PathVariable Long id, HttpSession session, Model model) {
        Operador operador = operadorAutenticado(session);
        if (operador == null) return "redirect:/login";
        Emergencia emergencia = operadorService.obtener(id).orElse(null);
        if (emergencia == null) return "redirect:/operador/emergencias";
        base(model, operador, "emergencias");
        model.addAttribute("emergencia", emergencia);
        model.addAttribute("evidencias", evidenciaService.listarPorEmergencia(id));
        model.addAttribute("ubicacion", ubicacionService.obtenerPorEmergencia(id).orElse(null));
        return "operador/detalle";
    }

    @PostMapping("/operador/emergencia/{id}/aceptar")
    public String aceptar(@PathVariable Long id, HttpSession session) {
        Operador operador = operadorAutenticado(session);
        if (operador == null) return "redirect:/login";
        operadorService.aceptar(id, operador);
        return "redirect:/operador/emergencia/" + id + "/clasificar";
    }

    @PostMapping("/operador/emergencia/{id}/rechazar")
    public String rechazar(@PathVariable Long id,
                           @RequestParam(required = false) String motivo,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {
        Operador operador = operadorAutenticado(session);
        if (operador == null) return "redirect:/login";
        operadorService.rechazar(id, operador, motivo);
        redirectAttributes.addFlashAttribute("exito", "Emergencia rechazada.");
        return "redirect:/operador/emergencias";
    }

    @GetMapping("/operador/emergencia/{id}/clasificar")
    public String clasificar(@PathVariable Long id, HttpSession session, Model model) {
        Operador operador = operadorAutenticado(session);
        if (operador == null) return "redirect:/login";
        Emergencia emergencia = operadorService.obtener(id).orElse(null);
        if (emergencia == null) return "redirect:/operador/emergencias";
        base(model, operador, "emergencias");
        model.addAttribute("emergencia", emergencia);
        model.addAttribute("tipos", TipoEmergencia.values());
        model.addAttribute("prioridades", PrioridadEmergencia.values());
        return "operador/clasificar";
    }

    @PostMapping("/operador/emergencia/{id}/clasificar")
    public String guardarClasificacion(@PathVariable Long id,
                                       @RequestParam TipoEmergencia tipo,
                                       @RequestParam(defaultValue = "MEDIA") PrioridadEmergencia prioridad,
                                       @RequestParam @NotBlank(message = "La descripción preliminar es obligatoria") String descripcionOperador,
                                       HttpSession session,
                                       RedirectAttributes redirectAttributes) {
        Operador operador = operadorAutenticado(session);
        if (operador == null) return "redirect:/login";
        if (descripcionOperador == null || descripcionOperador.isBlank()) {
            redirectAttributes.addFlashAttribute("error", "La descripcion preliminar es obligatoria.");
            return "redirect:/operador/emergencia/" + id + "/clasificar";
        }
        operadorService.clasificar(id, operador, tipo, prioridad, descripcionOperador);
        return "redirect:/operador/emergencia/" + id + "/seguimiento";
    }

    @GetMapping("/operador/emergencia/{id}/seguimiento")
    public String seguimiento(@PathVariable Long id, HttpSession session, Model model) {
        Operador operador = operadorAutenticado(session);
        if (operador == null) return "redirect:/login";
        Emergencia emergencia = operadorService.obtener(id).orElse(null);
        if (emergencia == null) return "redirect:/operador/emergencias";
        base(model, operador, "emergencias");
        model.addAttribute("emergencia", emergencia);
        model.addAttribute("evidencias", evidenciaService.listarPorEmergencia(id));
        return "operador/seguimiento";
    }

    @GetMapping("/operador/emergencia/{id}/notificar")
    public String notificar(@PathVariable Long id, HttpSession session, Model model) {
        Operador operador = operadorAutenticado(session);
        if (operador == null) return "redirect:/login";
        Emergencia emergencia = operadorService.obtener(id).orElse(null);
        if (emergencia == null) return "redirect:/operador/emergencias";
        String recomendada = notificacionService.entidadRecomendada(emergencia);
        base(model, operador, "emergencias");
        model.addAttribute("emergencia", emergencia);
        model.addAttribute("entidades", List.of("Comisaria Distrital", "SAMU", "Bomberos"));
        model.addAttribute("entidadRecomendada", recomendada);
        model.addAttribute("correoSugerido", notificacionService.correoSugerido(recomendada));
        return "operador/notificar";
    }

    @PostMapping("/operador/emergencia/{id}/notificar")
    public String enviarNotificacion(@PathVariable Long id,
                                     @RequestParam @NotBlank(message = "La entidad es obligatoria") String entidad,
                                     @RequestParam @NotBlank(message = "El correo de destino es obligatorio") @Email(message = "Debe ser un correo electrónico válido") String correoDestino,
                                     HttpSession session,
                                     RedirectAttributes redirectAttributes) {
        Operador operador = operadorAutenticado(session);
        if (operador == null) return "redirect:/login";
        Emergencia emergencia = operadorService.obtener(id).orElseThrow();
        boolean correoEnviado = notificacionService.notificarAutoridad(emergencia, operador, entidad, correoDestino);
        operadorService.registrarHistorial(emergencia, operador, "AUTORIDAD_NOTIFICADA", "Entidad: " + entidad);
        redirectAttributes.addFlashAttribute(
            "exito",
            correoEnviado
                ? "Autoridad notificada correctamente."
                : "Autoridad marcada como notificada. El correo no salio por credenciales SMTP, pero el caso ya figura en atencion y autoridad notificada."
        );
        return "redirect:/operador/emergencia/" + id + "/seguimiento";
    }

    @PostMapping("/operador/emergencia/{id}/finalizar")
    public String finalizar(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        Operador operador = operadorAutenticado(session);
        if (operador == null) return "redirect:/login";
        operadorService.finalizar(id, operador);
        redirectAttributes.addFlashAttribute("exito", "Emergencia finalizada.");
        return "redirect:/operador/historial";
    }

    @GetMapping("/operador/historial")
    public String historial(HttpSession session, Model model) {
        Operador operador = operadorAutenticado(session);
        if (operador == null) return "redirect:/login";
        base(model, operador, "historial");
        model.addAttribute("emergencias", operadorService.historialAtendido(operador));
        model.addAttribute("estados", List.of(EstadoEmergencia.AUTORIDAD_NOTIFICADA, EstadoEmergencia.RESUELTA, EstadoEmergencia.RECHAZADA));
        model.addAttribute("tipos", TipoEmergencia.values());
        return "operador/historial";
    }

    @GetMapping("/operador/perfil")
    public String perfil(HttpSession session, Model model) {
        Operador operador = operadorAutenticado(session);
        if (operador == null) return "redirect:/login";
        base(model, operador, "perfil");
        model.addAttribute("atendidas", operadorService.historialAtendido(operador).size());
        return "operador/perfil";
    }

    @GetMapping("/operador/camaras")
    public String monitoreoCamaras(HttpSession session, Model model) {
        Operador operador = operadorAutenticado(session);
        if (operador == null) return "redirect:/login";
        base(model, operador, "camaras");
        model.addAttribute("camaras", camaraService.listarActivas());
        model.addAttribute("todasLasCamaras", camaraService.listarTodas());
        model.addAttribute("camarasPostPath", "/operador/camaras");
        model.addAttribute("camarasQuickAddPath", "/operador/camaras/quick-add");
        model.addAttribute("camarasBasePath", "/operador/camaras");
        return "operador/monitoreo-camaras";
    }

    @PostMapping("/operador/camaras")
    public String crearCamara(@RequestParam @NotBlank(message = "El nombre de la cámara es obligatorio") String nombre,
                              @RequestParam(required = false) String ubicacion,
                              @RequestParam @NotBlank(message = "La URL del stream es obligatoria") String urlStream,
                              @RequestParam(defaultValue = "true") boolean activa,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        Operador operador = operadorAutenticado(session);
        if (operador == null) return "redirect:/login";
        Camara camara = new Camara();
        camara.setNombre(nombre);
        camara.setUbicacion(ubicacion);
        camara.setUrlStream(urlStream);
        camara.setActiva(activa);
        camaraService.guardar(camara);
        redirectAttributes.addFlashAttribute("exito", "Cámara registrada correctamente.");
        return "redirect:/operador/camaras";
    }

    @PostMapping(value = "/operador/camaras/quick-add", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Map<String, Object>> quickAddCamara(@RequestParam @NotBlank(message = "El nombre de la cámara es obligatorio") String nombre,
                                                              @RequestParam(required = false) String ubicacion,
                                                              @RequestParam @NotBlank(message = "La URL del stream es obligatoria") String urlStream,
                                                              @RequestParam(defaultValue = "true") boolean activa,
                                                              HttpSession session) {
        Operador operador = operadorAutenticado(session);
        SuperAdmin admin = adminAutenticado(session);
        if (operador == null && admin == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Sesión expirada"));
        }

        Camara camara = new Camara();
        camara.setNombre(nombre);
        camara.setUbicacion(ubicacion);
        camara.setUrlStream(urlStream);
        camara.setActiva(activa);
        Camara guardada = camaraService.guardar(camara);

        java.util.Map<String, Object> respuesta = new java.util.HashMap<>();
        respuesta.put("id", guardada.getId());
        respuesta.put("nombre", guardada.getNombre());
        respuesta.put("ubicacion", guardada.getUbicacion() != null ? guardada.getUbicacion() : "");
        respuesta.put("urlStream", guardada.getUrlStream());
        respuesta.put("activa", guardada.isActiva());
        return ResponseEntity.ok(respuesta);
    }

    @PostMapping("/operador/camaras/{id}/editar")
    public String editarCamara(@PathVariable Long id,
                               @RequestParam @NotBlank(message = "El nombre de la cámara es obligatorio") String nombre,
                               @RequestParam(required = false) String ubicacion,
                               @RequestParam @NotBlank(message = "La URL del stream es obligatoria") String urlStream,
                               @RequestParam(defaultValue = "true") boolean activa,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        Operador operador = operadorAutenticado(session);
        if (operador == null) return "redirect:/login";
        Camara camara = camaraService.obtener(id).orElse(null);
        if (camara == null) {
            redirectAttributes.addFlashAttribute("error", "La cámara no existe.");
            return "redirect:/operador/camaras";
        }
        camara.setNombre(nombre);
        camara.setUbicacion(ubicacion);
        camara.setUrlStream(urlStream);
        camara.setActiva(activa);
        camaraService.guardar(camara);
        redirectAttributes.addFlashAttribute("exito", "Cámara actualizada correctamente.");
        return "redirect:/operador/camaras";
    }

    @PostMapping("/operador/camaras/{id}/desactivar")
    public String desactivarCamara(@PathVariable Long id,
                                   HttpSession session,
                                   RedirectAttributes redirectAttributes) {
        Operador operador = operadorAutenticado(session);
        if (operador == null) return "redirect:/login";
        camaraService.desactivar(id);
        redirectAttributes.addFlashAttribute("exito", "Cámara desactivada.");
        return "redirect:/operador/camaras";
    }

    @PostMapping("/operador/camaras/{id}/eliminar")
    public String eliminarCamara(@PathVariable Long id,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        Operador operador = operadorAutenticado(session);
        if (operador == null) return "redirect:/login";
        camaraService.eliminar(id);
        redirectAttributes.addFlashAttribute("exito", "Cámara eliminada.");
        return "redirect:/operador/camaras";
    }

    // Guarda el video DIRECTO en la base de datos (campo "contenido" de Evidencia), sin carpetas ni servicios externos.
    private void guardarVideoEvidencia(MultipartFile video, Emergencia emergencia) throws IOException {
        Evidencia evidencia = new Evidencia();
        evidencia.setEmergencia(emergencia);
        evidencia.setUsuario(emergencia.getUsuario());
        evidencia.setTipo("VIDEO");
        evidencia.setNombreArchivo(video.getOriginalFilename() != null ? video.getOriginalFilename() : "video.mp4");
        evidencia.setContentType(video.getContentType());
        evidencia.setTamanoBytes(video.getSize());
        evidencia.setDescripcion("Video subido manualmente desde reporte telefónico");
        evidencia.setContenido(video.getBytes());
        evidencia.setRutaArchivo("pendiente"); // se completa abajo una vez que ya tenemos el id

        Evidencia guardada = evidenciaService.guardar(evidencia);
        guardada.setRutaArchivo("/evidencias/" + guardada.getId() + "/archivo");
        evidenciaService.guardar(guardada);
    }

    private void base(Model model, Operador operador, String active) {
        model.addAttribute("operador", operador);
        model.addAttribute("active", active);
    }

    private Operador operadorAutenticado(HttpSession session) {
        return (Operador) session.getAttribute("operador");
    }

    private SuperAdmin adminAutenticado(HttpSession session) {
        return (SuperAdmin) session.getAttribute("admin");
    }
}