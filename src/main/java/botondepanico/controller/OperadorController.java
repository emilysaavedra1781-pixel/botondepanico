package botondepanico.controller;

import botondepanico.model.*;
import botondepanico.service.EvidenciaService;
import botondepanico.service.NotificacionService;
import botondepanico.service.OperadorService;
import botondepanico.service.UbicacionService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;

@Controller
public class OperadorController {

    private final OperadorService operadorService;
    private final EvidenciaService evidenciaService;
    private final UbicacionService ubicacionService;
    private final NotificacionService notificacionService;

    public OperadorController(OperadorService operadorService,
                              EvidenciaService evidenciaService,
                              UbicacionService ubicacionService,
                              NotificacionService notificacionService) {
        this.operadorService = operadorService;
        this.evidenciaService = evidenciaService;
        this.ubicacionService = ubicacionService;
        this.notificacionService = notificacionService;
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
                                       @RequestParam String descripcionOperador,
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
                                     @RequestParam String entidad,
                                     @RequestParam String correoDestino,
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

    private void base(Model model, Operador operador, String active) {
        model.addAttribute("operador", operador);
        model.addAttribute("active", active);
    }

    private Operador operadorAutenticado(HttpSession session) {
        return (Operador) session.getAttribute("operador");
    }
}
