package botondepanico.controller;

import botondepanico.model.*;
import botondepanico.service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;

@Controller
public class AdminController {

    private final AdminService adminService;
    private final MonitoreoService monitoreoService;
    private final OperadorService operadorService;
    private final UsuarioService usuarioService;
    private final EvidenciaService evidenciaService;
    private final EstadisticaService estadisticaService;
    private final ReporteService reporteService;
    private final ConfiguracionService configuracionService;

    public AdminController(AdminService adminService,
                           MonitoreoService monitoreoService,
                           OperadorService operadorService,
                           UsuarioService usuarioService,
                           EvidenciaService evidenciaService,
                           EstadisticaService estadisticaService,
                           ReporteService reporteService,
                           ConfiguracionService configuracionService) {
        this.adminService = adminService;
        this.monitoreoService = monitoreoService;
        this.operadorService = operadorService;
        this.usuarioService = usuarioService;
        this.evidenciaService = evidenciaService;
        this.estadisticaService = estadisticaService;
        this.reporteService = reporteService;
        this.configuracionService = configuracionService;
    }

    @GetMapping("/admin/dashboard")
    public String dashboard(HttpSession session, Model model) {
        SuperAdmin admin = adminAutenticado(session);
        if (admin == null) return "redirect:/login";
        base(model, admin, "dashboard");
        agregarMetricas(model);
        agregarGraficos(model);
        return "admin/dashboard";
    }

    @GetMapping("/admin/monitoreo")
    public String monitoreo(HttpSession session, Model model) {
        SuperAdmin admin = adminAutenticado(session);
        if (admin == null) return "redirect:/login";
        base(model, admin, "monitoreo");
        List<Emergencia> emergencias = monitoreoService.emergenciasActivas();
        model.addAttribute("emergencias", emergencias);
        model.addAttribute("operadores", monitoreoService.operadoresActivos());
        model.addAttribute("activas", emergencias.size());
        model.addAttribute("usuariosSeguimiento", emergencias.stream().map(e -> e.getUsuario().getId()).distinct().count());
        model.addAttribute("camarasActivas", emergenciaConEvidencia(emergencias));
        return "admin/monitoreo";
    }

    @GetMapping("/admin/operadores")
    public String operadores(HttpSession session, Model model) {
        SuperAdmin admin = adminAutenticado(session);
        if (admin == null) return "redirect:/login";
        base(model, admin, "operadores");
        model.addAttribute("operadores", usuarioService.listarOperadores());
        return "admin/operadores";
    }

    @PostMapping("/admin/operadores/{id}/aprobar")
    public String aprobarOperador(@PathVariable Long id, RedirectAttributes redirectAttributes, HttpSession session) {
        if (adminAutenticado(session) == null) return "redirect:/login";
        usuarioService.aprobarOperador(id);
        redirectAttributes.addFlashAttribute("exito", "Operador aprobado correctamente.");
        return "redirect:/admin/operadores";
    }

    @PostMapping("/admin/operadores/{id}/rechazar")
    public String rechazarOperador(@PathVariable Long id, RedirectAttributes redirectAttributes, HttpSession session) {
        if (adminAutenticado(session) == null) return "redirect:/login";
        usuarioService.rechazarOperador(id);
        redirectAttributes.addFlashAttribute("exito", "Operador rechazado.");
        return "redirect:/admin/operadores";
    }

    @GetMapping("/admin/usuarios")
    public String usuarios(HttpSession session, Model model) {
        SuperAdmin admin = adminAutenticado(session);
        if (admin == null) return "redirect:/login";
        base(model, admin, "usuarios");
        model.addAttribute("usuarios", usuarioService.listarUsuarios());
        return "admin/usuarios";
    }

    @PostMapping("/admin/usuarios/{id}/bloquear")
    public String bloquearUsuario(@PathVariable Long id, RedirectAttributes redirectAttributes, HttpSession session) {
        if (adminAutenticado(session) == null) return "redirect:/login";
        usuarioService.bloquearUsuario(id);
        redirectAttributes.addFlashAttribute("exito", "Usuario bloqueado.");
        return "redirect:/admin/usuarios";
    }

    @PostMapping("/admin/usuarios/{id}/activar")
    public String activarUsuario(@PathVariable Long id, RedirectAttributes redirectAttributes, HttpSession session) {
        if (adminAutenticado(session) == null) return "redirect:/login";
        usuarioService.activarUsuario(id);
        redirectAttributes.addFlashAttribute("exito", "Usuario activado.");
        return "redirect:/admin/usuarios";
    }

    @GetMapping("/admin/emergencias")
    public String emergencias(HttpSession session, Model model) {
        SuperAdmin admin = adminAutenticado(session);
        if (admin == null) return "redirect:/login";
        base(model, admin, "emergencias");
        model.addAttribute("emergencias", operadorService.buscar(null, null, null, null, null));
        model.addAttribute("estados", List.of(EstadoEmergencia.PENDIENTE, EstadoEmergencia.EN_ATENCION, EstadoEmergencia.AUTORIDAD_NOTIFICADA, EstadoEmergencia.RESUELTA, EstadoEmergencia.RECHAZADA));
        model.addAttribute("tipos", TipoEmergencia.values());
        return "admin/emergencias";
    }

    @GetMapping("/admin/emergencias/{id}")
    public String detalleEmergencia(@PathVariable Long id, HttpSession session, Model model) {
        SuperAdmin admin = adminAutenticado(session);
        if (admin == null) return "redirect:/login";
        Emergencia emergencia = operadorService.obtener(id).orElse(null);
        if (emergencia == null) return "redirect:/admin/emergencias";
        base(model, admin, "emergencias");
        model.addAttribute("emergencia", emergencia);
        model.addAttribute("evidencias", evidenciaService.listarPorEmergencia(id));
        return "admin/detalle-emergencia";
    }

    @GetMapping("/admin/historial")
    public String historial(HttpSession session, Model model) {
        SuperAdmin admin = adminAutenticado(session);
        if (admin == null) return "redirect:/login";
        base(model, admin, "historial");
        model.addAttribute("emergencias", operadorService.buscar(null, null, null, null, null));
        return "admin/historial";
    }

    @GetMapping("/admin/estadisticas")
    public String estadisticas(HttpSession session, Model model) {
        SuperAdmin admin = adminAutenticado(session);
        if (admin == null) return "redirect:/login";
        base(model, admin, "estadisticas");
        agregarMetricas(model);
        agregarGraficos(model);
        return "admin/estadisticas";
    }

    @PostMapping("/admin/estadisticas/reporte")
    public String generarReporte(@RequestParam(required = false) String periodo,
                                 @RequestParam(required = false) String distrito,
                                 @RequestParam(required = false) String tipo,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        SuperAdmin admin = adminAutenticado(session);
        if (admin == null) return "redirect:/login";
        reporteService.registrar(periodo, distrito, tipo, admin);
        redirectAttributes.addFlashAttribute("exito", "Reporte generado y registrado.");
        return "redirect:/admin/estadisticas";
    }

    @GetMapping("/admin/configuracion")
    public String configuracion(HttpSession session, Model model) {
        SuperAdmin admin = adminAutenticado(session);
        if (admin == null) return "redirect:/login";
        base(model, admin, "configuracion");
        model.addAttribute("config", configuracionService.obtener());
        return "admin/configuracion";
    }

    @PostMapping("/admin/configuracion")
    public String guardarConfiguracion(@ModelAttribute ConfiguracionSistema config,
                                       HttpSession session,
                                       RedirectAttributes redirectAttributes) {
        if (adminAutenticado(session) == null) return "redirect:/login";
        configuracionService.guardar(config);
        redirectAttributes.addFlashAttribute("exito", "Configuracion guardada correctamente.");
        return "redirect:/admin/configuracion";
    }

    private void agregarMetricas(Model model) {
        model.addAttribute("emergenciasActivas", adminService.emergenciasActivas());
        model.addAttribute("emergenciasHoy", adminService.emergenciasHoy());
        model.addAttribute("emergenciasMes", adminService.emergenciasMes());
        model.addAttribute("usuariosRegistrados", adminService.usuariosRegistrados());
        model.addAttribute("operadoresEnLinea", adminService.operadoresEnLinea());
        model.addAttribute("promedioAtencion", "18 min");
        model.addAttribute("resueltas", adminService.emergenciasResueltas());
        model.addAttribute("entidadesNotificadas", adminService.entidadesNotificadas());
    }

    private void agregarGraficos(Model model) {
        model.addAttribute("porTipo", estadisticaService.porTipo());
        model.addAttribute("porDistrito", estadisticaService.porDistrito());
        model.addAttribute("porHora", estadisticaService.porHora());
        model.addAttribute("porOperador", estadisticaService.porOperador());
    }

    private long emergenciaConEvidencia(List<Emergencia> emergencias) {
        return emergencias.stream().filter(e -> e.getEvidencias() != null && !e.getEvidencias().isEmpty()).count();
    }

    private void base(Model model, SuperAdmin admin, String active) {
        model.addAttribute("admin", admin);
        model.addAttribute("active", active);
    }

    private SuperAdmin adminAutenticado(HttpSession session) {
        return (SuperAdmin) session.getAttribute("admin");
    }
}
