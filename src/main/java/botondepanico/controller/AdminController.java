package botondepanico.controller;

import botondepanico.dto.AuthResponseDTO;
import botondepanico.dto.EmergenciaResumenDTO;
import botondepanico.model.*;
import botondepanico.repository.SuperAdminRepository;
import botondepanico.service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Map;

@Controller
@Validated
public class AdminController {

    private final AdminService adminService;
    private final MonitoreoService monitoreoService;
    private final OperadorService operadorService;
    private final UsuarioService usuarioService;
    private final EvidenciaService evidenciaService;
    private final EstadisticaService estadisticaService;
    private final ReporteService reporteService;
    private final ConfiguracionService configuracionService;
    private final CamaraService camaraService;
    private final SuperAdminRepository adminRepository;

    public AdminController(AdminService adminService,
                           MonitoreoService monitoreoService,
                           OperadorService operadorService,
                           UsuarioService usuarioService,
                           EvidenciaService evidenciaService,
                           EstadisticaService estadisticaService,
                           ReporteService reporteService,
                           ConfiguracionService configuracionService,
                           CamaraService camaraService,
                           SuperAdminRepository adminRepository) {
        this.adminService = adminService;
        this.monitoreoService = monitoreoService;
        this.operadorService = operadorService;
        this.usuarioService = usuarioService;
        this.evidenciaService = evidenciaService;
        this.estadisticaService = estadisticaService;
        this.reporteService = reporteService;
        this.configuracionService = configuracionService;
        this.camaraService = camaraService;
        this.adminRepository = adminRepository;
    }

    @GetMapping("/admin/dashboard")
    public String dashboard(HttpSession session, Model model) {
        AuthResponseDTO auth = adminAutenticado(session);
        if (auth == null) return "redirect:/login";
        base(model, auth, "dashboard");
        agregarMetricas(model);
        agregarGraficos(model);
        return "admin/dashboard";
    }

    @GetMapping("/admin/monitoreo")
    public String monitoreo(HttpSession session, Model model) {
        AuthResponseDTO auth = adminAutenticado(session);
        if (auth == null) return "redirect:/login";
        base(model, auth, "monitoreo");
        List<EmergenciaResumenDTO> emergencias = monitoreoService.emergenciasActivas();
        model.addAttribute("emergencias", emergencias);
        model.addAttribute("operadores", monitoreoService.operadoresActivos());
        model.addAttribute("activas", emergencias.size());
        model.addAttribute("usuariosSeguimiento", emergencias.stream().map(EmergenciaResumenDTO::getUsuarioId).distinct().count());
        model.addAttribute("camarasActivas", 0); // Need to re-evaluate this logic if needed
        return "admin/monitoreo";
    }

@GetMapping("/admin/camaras")
public String monitoreoCamaras(HttpSession session, Model model) {
    AuthResponseDTO auth = adminAutenticado(session);
    if (auth == null) return "redirect:/login";
    base(model, auth, "camaras");
    model.addAttribute("camaras", camaraService.listarActivas());
    model.addAttribute("todasLasCamaras", camaraService.listarTodas());
    model.addAttribute("camarasPostPath", "/admin/camaras");
    model.addAttribute("camarasQuickAddPath", "/admin/camaras/quick-add");
    model.addAttribute("camarasBasePath", "/admin/camaras");
    return "admin/monitoreo-camaras";
}

    @PostMapping("/admin/camaras")
    public String crearCamara(@RequestParam @NotBlank(message = "El nombre de la cámara es obligatorio") String nombre,
                              @RequestParam(required = false) String ubicacion,
                              @RequestParam @NotBlank(message = "La URL del stream es obligatoria") String urlStream,
                              @RequestParam(defaultValue = "true") boolean activa,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        if (adminAutenticado(session) == null) return "redirect:/login";
        Camara camara = new Camara();
        camara.setNombre(nombre);
        camara.setUbicacion(ubicacion);
        camara.setUrlStream(urlStream);
        camara.setActiva(activa);
        camaraService.guardar(camara);
        redirectAttributes.addFlashAttribute("exito", "Cámara registrada correctamente.");
        return "redirect:/admin/camaras";
    }

    @PostMapping(value = "/admin/camaras/quick-add", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Map<String, Object>> quickAddCamara(@RequestParam @NotBlank(message = "El nombre de la cámara es obligatorio") String nombre,
                                                              @RequestParam(required = false) String ubicacion,
                                                              @RequestParam @NotBlank(message = "La URL del stream es obligatoria") String urlStream,
                                                              @RequestParam(defaultValue = "true") boolean activa,
                                                              HttpSession session) {
        AuthResponseDTO auth = adminAutenticado(session);
        if (auth == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Sesión expirada"));
        }

        Camara camara = new Camara();
        camara.setNombre(nombre);
        camara.setUbicacion(ubicacion);
        camara.setUrlStream(urlStream);
        camara.setActiva(activa);
        Camara guardada = camaraService.guardar(camara);

        return ResponseEntity.ok(Map.of(
            "id", guardada.getId(),
            "nombre", guardada.getNombre(),
            "ubicacion", guardada.getUbicacion() != null ? guardada.getUbicacion() : "",
            "urlStream", guardada.getUrlStream(),
            "activa", guardada.isActiva()
        ));
    }

    @PostMapping("/admin/camaras/{id}/editar")
    public String editarCamara(@PathVariable Long id,
                               @RequestParam @NotBlank(message = "El nombre de la cámara es obligatorio") String nombre,
                               @RequestParam(required = false) String ubicacion,
                               @RequestParam @NotBlank(message = "La URL del stream es obligatoria") String urlStream,
                               @RequestParam(defaultValue = "true") boolean activa,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        if (adminAutenticado(session) == null) return "redirect:/login";
        Camara camara = camaraService.obtener(id).orElse(null);
        if (camara == null) {
            redirectAttributes.addFlashAttribute("error", "La cámara no existe.");
            return "redirect:/admin/camaras";
        }
        camara.setNombre(nombre);
        camara.setUbicacion(ubicacion);
        camara.setUrlStream(urlStream);
        camara.setActiva(activa);
        camaraService.guardar(camara);
        redirectAttributes.addFlashAttribute("exito", "Cámara actualizada correctamente.");
        return "redirect:/admin/camaras";
    }

    @PostMapping("/admin/camaras/{id}/desactivar")
    public String desactivarCamara(@PathVariable Long id,
                                   HttpSession session,
                                   RedirectAttributes redirectAttributes) {
        if (adminAutenticado(session) == null) return "redirect:/login";
        camaraService.desactivar(id);
        redirectAttributes.addFlashAttribute("exito", "Cámara desactivada.");
        return "redirect:/admin/camaras";
    }

    @PostMapping("/admin/camaras/{id}/eliminar")
    public String eliminarCamara(@PathVariable Long id,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        if (adminAutenticado(session) == null) return "redirect:/login";
        camaraService.eliminar(id);
        redirectAttributes.addFlashAttribute("exito", "Cámara eliminada.");
        return "redirect:/admin/camaras";
    }

    @GetMapping("/admin/operadores")
    public String operadores(HttpSession session, Model model) {
        AuthResponseDTO auth = adminAutenticado(session);
        if (auth == null) return "redirect:/login";
        base(model, auth, "operadores");
        model.addAttribute("operadores", usuarioService.listarOperadores());
        return "admin/operadores";
    }

    @PostMapping("/admin/operadores/crear")
    public String crearOperador(@RequestParam @NotBlank(message = "El nombre completo es obligatorio") String nombreCompleto,
                                @RequestParam @NotBlank(message = "El DNI es obligatorio") @Pattern(regexp = "\\d{8}", message = "El DNI debe tener 8 dígitos") String dni,
                                @RequestParam @NotBlank(message = "El celular es obligatorio") @Pattern(regexp = "\\d{9}", message = "El celular debe tener 9 dígitos") String celular,
                                @RequestParam @NotBlank(message = "El correo es obligatorio") @Email(message = "Debe ser un correo electrónico válido") String correo,
                                @RequestParam @NotBlank(message = "La contraseña es obligatoria") @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres") String password,
                                @RequestParam String confirmarPassword,
                                @RequestParam(required = false) String distrito,
                                RedirectAttributes redirectAttributes,
                                HttpSession session) {
        if (adminAutenticado(session) == null) return "redirect:/login";

        if (password == null || password.isBlank() || !password.equals(confirmarPassword)) {
            redirectAttributes.addFlashAttribute("error", "Las contraseñas no coinciden o están vacías.");
            return "redirect:/admin/operadores";
        }

        Operador operador = usuarioService.crearOperadorPorAdmin(nombreCompleto, dni, celular, correo, password, distrito);
        if (operador == null) {
            redirectAttributes.addFlashAttribute("error", "No se pudo crear el operador. Verifica que el correo, DNI o celular no estén registrados.");
            return "redirect:/admin/operadores";
        }

        redirectAttributes.addFlashAttribute("exito", "Operador creado correctamente.");
        return "redirect:/admin/operadores";
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
        AuthResponseDTO auth = adminAutenticado(session);
        if (auth == null) return "redirect:/login";
        base(model, auth, "usuarios");
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
        AuthResponseDTO auth = adminAutenticado(session);
        if (auth == null) return "redirect:/login";
        base(model, auth, "emergencias");
        model.addAttribute("emergencias", operadorService.buscar(null, null, null, null, null));
        model.addAttribute("estados", List.of(EstadoEmergencia.PENDIENTE, EstadoEmergencia.EN_ATENCION, EstadoEmergencia.AUTORIDAD_NOTIFICADA, EstadoEmergencia.RESUELTA, EstadoEmergencia.RECHAZADA));
        model.addAttribute("tipos", TipoEmergencia.values());
        return "admin/emergencias";
    }

    @GetMapping("/admin/emergencias/{id}")
    public String detalleEmergencia(@PathVariable Long id, HttpSession session, Model model) {
        AuthResponseDTO auth = adminAutenticado(session);
        if (auth == null) return "redirect:/login";
        Emergencia emergencia = operadorService.obtener(id).orElse(null);
        if (emergencia == null) return "redirect:/admin/emergencias";
        base(model, auth, "emergencias");
        model.addAttribute("emergencia", emergencia);
        model.addAttribute("evidencias", evidenciaService.listarPorEmergencia(id));
        return "admin/detalle-emergencia";
    }

    @GetMapping("/admin/historial")
    public String historial(HttpSession session, Model model) {
        AuthResponseDTO auth = adminAutenticado(session);
        if (auth == null) return "redirect:/login";
        base(model, auth, "historial");
        model.addAttribute("emergencias", operadorService.buscar(null, null, null, null, null));
        return "admin/historial";
    }

    @GetMapping("/admin/estadisticas")
    public String estadisticas(HttpSession session, Model model) {
        AuthResponseDTO auth = adminAutenticado(session);
        if (auth == null) return "redirect:/login";
        base(model, auth, "estadisticas");
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
        AuthResponseDTO auth = adminAutenticado(session);
        if (auth == null) return "redirect:/login";
        SuperAdmin admin = adminRepository.findById(auth.getId()).orElse(null);
        if (admin == null) return "redirect:/logout";
        reporteService.registrar(periodo, distrito, tipo, admin);
        redirectAttributes.addFlashAttribute("exito", "Reporte generado y registrado.");
        return "redirect:/admin/estadisticas";
    }

    @GetMapping("/admin/configuracion")
    public String configuracion(HttpSession session, Model model) {
        AuthResponseDTO auth = adminAutenticado(session);
        if (auth == null) return "redirect:/login";
        base(model, auth, "configuracion");
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

    private void base(Model model, AuthResponseDTO auth, String active) {
        model.addAttribute("admin", auth);
        model.addAttribute("active", active);
    }

    private AuthResponseDTO adminAutenticado(HttpSession session) {
        return (AuthResponseDTO) session.getAttribute("admin");
    }
}
