package botondepanico.controller;

import botondepanico.dto.AuthResponseDTO;
import botondepanico.model.Emergencia;
import botondepanico.model.Evidencia;
import botondepanico.model.Operador;
import botondepanico.model.SuperAdmin;
import botondepanico.model.Usuario;
import botondepanico.repository.UsuarioRepository;
import botondepanico.service.UsuarioModuloService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
public class UsuarioController {

    @Autowired
    private UsuarioModuloService usuarioModuloService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping("/usuario/dashboard")
    public String dashboard(HttpSession session, Model model) {
        AuthResponseDTO auth = usuarioAutenticado(session);
        if (auth == null) return "redirect:/login";

        Usuario usuario = usuarioRepository.findById(auth.getId()).orElse(null);
        if (usuario == null) return "redirect:/logout";

        Optional<Emergencia> activa = usuarioModuloService.obtenerEmergenciaActiva(usuario);
        model.addAttribute("usuario", auth);
        model.addAttribute("emergenciaActiva", activa.orElse(null));
        return "usuario/dashboard";
    }

    @GetMapping("/usuario/emergencia-activa")
    public String emergenciaActiva(HttpSession session, Model model) {
        AuthResponseDTO auth = usuarioAutenticado(session);
        if (auth == null) return "redirect:/login";

        Usuario usuario = usuarioRepository.findById(auth.getId()).orElse(null);
        if (usuario == null) return "redirect:/logout";

        Optional<Emergencia> activa = usuarioModuloService.obtenerUltimaEmergencia(usuario);
        model.addAttribute("usuario", auth);
        model.addAttribute("emergencia", activa.orElse(null));
        return "usuario/emergencia-activa";
    }

    @GetMapping("/usuario/evidencias")
    public String evidencias(HttpSession session, Model model) {
        AuthResponseDTO auth = usuarioAutenticado(session);
        if (auth == null) return "redirect:/login";

        Usuario usuario = usuarioRepository.findById(auth.getId()).orElse(null);
        if (usuario == null) return "redirect:/logout";

        model.addAttribute("usuario", auth);
        model.addAttribute("emergencia", usuarioModuloService.obtenerUltimaEmergencia(usuario).orElse(null));
        model.addAttribute("emergencias", usuarioModuloService.listarHistorial(usuario));
        model.addAttribute("evidencias", usuarioModuloService.listarEvidenciasUsuario(usuario));
        return "usuario/evidencias";
    }

    @GetMapping("/usuario/historial")
    public String historial(HttpSession session, Model model) {
        AuthResponseDTO auth = usuarioAutenticado(session);
        if (auth == null) return "redirect:/login";

        Usuario usuario = usuarioRepository.findById(auth.getId()).orElse(null);
        if (usuario == null) return "redirect:/logout";

        List<Emergencia> emergencias = usuarioModuloService.listarHistorial(usuario);
        model.addAttribute("usuario", auth);
        model.addAttribute("emergencias", emergencias);
        return "usuario/historial";
    }

    @GetMapping("/usuario/emergencia/{id}")
    public String detalleEmergencia(@PathVariable Long id, HttpSession session, Model model) {
        AuthResponseDTO auth = usuarioAutenticado(session);
        if (auth == null) return "redirect:/login";

        Usuario usuario = usuarioRepository.findById(auth.getId()).orElse(null);
        if (usuario == null) return "redirect:/logout";

        Optional<Emergencia> emergencia = usuarioModuloService.obtenerEmergenciaDeUsuario(id, usuario);
        if (emergencia.isEmpty()) return "redirect:/usuario/historial";

        model.addAttribute("usuario", auth);
        model.addAttribute("emergencia", emergencia.get());
        model.addAttribute("evidencias", emergencia.get().getEvidencias());
        return "usuario/detalle-emergencia";
    }

    @GetMapping("/usuario/perfil")
    public String perfil(HttpSession session, Model model) {
        AuthResponseDTO auth = usuarioAutenticado(session);
        if (auth == null) return "redirect:/login";

        model.addAttribute("usuario", auth);
        return "usuario/perfil";
    }

    @PostMapping("/usuario/activar-sos")
    public String activarSos(@RequestParam String latitud,
                             @RequestParam String longitud,
                             @RequestParam(required = false) String distrito,
                             @RequestParam(required = false) String direccion,
                             @RequestParam(required = false) String tipoEmergencia,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        AuthResponseDTO auth = usuarioAutenticado(session);
        if (auth == null) return "redirect:/login";

        Usuario usuario = usuarioRepository.findById(auth.getId()).orElse(null);
        if (usuario == null) return "redirect:/logout";

        usuarioModuloService.activarSos(usuario, latitud, longitud, distrito, direccion, tipoEmergencia);
        redirectAttributes.addFlashAttribute("exito", "Emergencia registrada correctamente");
        return "redirect:/usuario/emergencia-activa";
    }

    @PostMapping("/usuario/subir-evidencia")
    public String subirEvidencia(@RequestParam("archivo") MultipartFile archivo,
                                 @RequestParam(required = false) String tipo,
                                 @RequestParam(required = false) String latitud,
                                 @RequestParam(required = false) String longitud,
                                 @RequestParam(required = false) String direccion,
                                 @RequestParam(required = false) String descripcion,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        AuthResponseDTO auth = usuarioAutenticado(session);
        if (auth == null) return "redirect:/login";

        Usuario usuario = usuarioRepository.findById(auth.getId()).orElse(null);
        if (usuario == null) return "redirect:/logout";

        try {
            Evidencia evidencia = usuarioModuloService.guardarEvidencia(usuario, archivo, tipo, latitud, longitud, direccion, descripcion);
            redirectAttributes.addFlashAttribute("exito", "Evidencia enviada: " + evidencia.getNombreArchivo());
        } catch (IOException | RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/usuario/evidencias";
    }

    @PostMapping("/usuario/subir-evidencia-json")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> subirEvidenciaJson(@RequestParam("archivo") MultipartFile archivo,
                                                                  @RequestParam(required = false) String tipo,
                                                                  @RequestParam(required = false) String latitud,
                                                                  @RequestParam(required = false) String longitud,
                                                                  @RequestParam(required = false) String direccion,
                                                                  @RequestParam(required = false) String descripcion,
                                                                  @RequestParam(required = false) Long emergenciaId,
                                                                  @RequestParam(defaultValue = "false") boolean nuevoCaso,
                                                                  HttpSession session) {
        AuthResponseDTO auth = usuarioAutenticado(session);
        if (auth == null) return ResponseEntity.status(401).build();

        Usuario usuario = usuarioRepository.findById(auth.getId()).orElse(null);
        if (usuario == null) return ResponseEntity.status(401).build();

        try {
            Evidencia evidencia = usuarioModuloService.guardarEvidencia(
                usuario,
                archivo,
                tipo,
                latitud,
                longitud,
                direccion,
                descripcion,
                emergenciaId,
                nuevoCaso
            );
            Map<String, Object> body = new HashMap<>();
            body.put("id", evidencia.getId());
            body.put("nombreArchivo", evidencia.getNombreArchivo());
            body.put("tipo", evidencia.getTipo());
            body.put("emergenciaId", evidencia.getEmergencia().getId());
            body.put("estado", evidencia.getEmergencia().getEstado().name());
            return ResponseEntity.ok(body);
        } catch (IOException | RuntimeException e) {
            Map<String, Object> body = new HashMap<>();
            body.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(body);
        }
    }

    @GetMapping("/usuario/evidencia/{id}/archivo")
    public ResponseEntity<Resource> archivoEvidencia(@PathVariable Long id, HttpSession session) {
        AuthResponseDTO auth = usuarioAutenticado(session);
        AuthResponseDTO operador = (AuthResponseDTO) session.getAttribute("operador");
        AuthResponseDTO admin = (AuthResponseDTO) session.getAttribute("admin");
        Optional<Evidencia> evidencia = Optional.empty();
        
        if (auth != null) {
            Usuario usuario = usuarioRepository.findById(auth.getId()).orElse(null);
            if (usuario != null) {
                evidencia = usuarioModuloService.obtenerEvidenciaVisible(id, usuario);
            }
        } else if (operador != null) {
            // Operador logic might need adjustment if it needs entity
            // For now, let's keep it consistent or fix as needed
        } else if (admin != null) {
            evidencia = usuarioModuloService.obtenerEvidenciaVisible(id, new SuperAdmin()); // Placeholder for admin logic
        } else {
            return ResponseEntity.status(401).build();
        }
        
        // Refined logic below...

        if (evidencia.isEmpty()) return ResponseEntity.notFound().build();

        Path ruta = Paths.get(evidencia.get().getRutaArchivo());
        Resource recurso = new PathResource(ruta);
        if (!recurso.exists() || !recurso.isReadable()) {
            return ResponseEntity.notFound().build();
        }

        String contentType = evidencia.get().getContentType() == null
            ? MediaType.APPLICATION_OCTET_STREAM_VALUE
            : evidencia.get().getContentType();

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + evidencia.get().getNombreArchivo() + "\"")
            .contentType(MediaType.parseMediaType(contentType))
            .body(recurso);
    }

    private AuthResponseDTO usuarioAutenticado(HttpSession session) {
        return (AuthResponseDTO) session.getAttribute("usuario");
    }
}
