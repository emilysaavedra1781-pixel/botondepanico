package botondepanico.controller;

import botondepanico.dto.AuthResponseDTO;
import botondepanico.dto.RegistroUsuarioDTO;
import botondepanico.model.EstadoOperador;
import botondepanico.model.EstadoUsuario;
import botondepanico.model.Operador;
import botondepanico.model.SuperAdmin;
import botondepanico.model.Usuario;
import botondepanico.service.UsuarioService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Controller
@Validated
public class AuthController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @PostMapping("/login")
    public String procesarLogin(@RequestParam @NotBlank(message = "El correo es obligatorio") @Email(message = "Debe ser un correo electrónico válido") String correo,
                                @RequestParam @NotBlank(message = "La contraseña es obligatoria") String contrasena,
                                HttpSession session,
                                Model model) {
        Usuario usuario = usuarioService.loginPorCorreo(correo, contrasena);
        if (usuario != null && "USUARIO".equalsIgnoreCase(usuario.getRol())) {
            if ("BLOQUEADO".equalsIgnoreCase(usuario.getEstadoCuenta())) {
                model.addAttribute("error", "Tu cuenta no se encuentra habilitada para acceder");
                return "login";
            }
            session.setAttribute("usuario", AuthResponseDTO.fromUsuario(usuario));
            return "redirect:/usuario/dashboard";
        }

        Operador operador = usuarioService.loginOperadorPorCorreo(correo, contrasena);
        if (operador != null) {
            if (operador.getEstado() == EstadoOperador.PENDIENTE) {
                return "redirect:/operador-pendiente";
            }
            if (operador.getEstado() == EstadoOperador.RECHAZADO || operador.getEstado() == EstadoOperador.BLOQUEADO) {
                model.addAttribute("error", "Tu cuenta no se encuentra habilitada para acceder");
                return "login";
            }
            session.setAttribute("operador", AuthResponseDTO.fromOperador(operador));
            return "redirect:/operador/dashboard";
        }

        SuperAdmin admin = usuarioService.loginSuperAdminPorCorreo(correo, contrasena);
        if (admin != null) {
            if (admin.getEstado() == EstadoUsuario.BLOQUEADO || admin.getEstado() == EstadoUsuario.INACTIVO) {
                model.addAttribute("error", "Tu cuenta no se encuentra habilitada para acceder");
                return "login";
            }
            session.setAttribute("admin", AuthResponseDTO.fromAdmin(admin));
            return "redirect:/admin/dashboard";
        }

        model.addAttribute("error", "Correo o contrasena incorrectos");
        return "login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    @GetMapping("/registro")
    public String seleccionRegistro() {
        return "registro";
    }

    @GetMapping("/registro-usuario")
    public String registroUsuario(Model model) {
        model.addAttribute("registroDTO", new RegistroUsuarioDTO());
        return "registro-usuario";
    }

    @PostMapping("/registro-usuario")
    public String registrarUsuario(@Valid @ModelAttribute("registroDTO") RegistroUsuarioDTO registroDTO,
                                   BindingResult result,
                                   HttpSession session,
                                   Model model) {
        if (result.hasErrors()) {
            return "registro-usuario";
        }

        if (!registroDTO.getPassword().equals(registroDTO.getConfirmarPassword())) {
            model.addAttribute("error", "Las contrasenas no coinciden");
            return "registro-usuario";
        }

        Usuario usuario = crearUsuarioBase(registroDTO.getNombreCompleto(), registroDTO.getDni(), registroDTO.getCelular(), registroDTO.getCorreo(), registroDTO.getPassword());
        usuario.setDistrito(registroDTO.getDistrito());

        Usuario guardado = usuarioService.registrarUsuario(usuario);
        if (guardado == null) {
            model.addAttribute("error", "El celular, DNI o correo ya esta registrado");
            return "registro-usuario";
        }

        session.setAttribute("usuario", AuthResponseDTO.fromUsuario(guardado));
        return "redirect:/usuario/dashboard";
    }

    @GetMapping("/registro-operador")
    public String registroOperador() {
        return "registro-operador-deshabilitado";
    }

    @PostMapping("/registro-operador")
    public String registrarOperador(@RequestParam String nombreCompleto,
                                    @RequestParam String dni,
                                    @RequestParam String celular,
                                    @RequestParam String correo,
                                    @RequestParam String password,
                                    @RequestParam String confirmarPassword,
                                    Model model) {
        model.addAttribute("mensaje", "El registro público de operadores ya no está disponible. Contacte a un administrador para obtener acceso.");
        return "registro-operador-deshabilitado";
    }

    @GetMapping("/operador-pendiente")
    public String operadorPendiente() {
        return "operador-pendiente";
    }

    private Usuario crearUsuarioBase(String nombreCompleto, String dni, String celular, String correo, String password) {
        String limpio = nombreCompleto == null ? "" : nombreCompleto.trim().replaceAll("\\s+", " ");
        String[] partes = limpio.split(" ", 2);

        Usuario usuario = new Usuario();
        usuario.setNombre(partes.length > 0 ? partes[0] : "");
        usuario.setApellido(partes.length > 1 ? partes[1] : "");
        usuario.setDni(dni);
        usuario.setCelular(celular);
        usuario.setCorreo(correo);
        usuario.setPassword(password);
        return usuario;
    }

    private Operador crearOperadorBase(String nombreCompleto, String dni, String celular, String correo, String password) {
        String limpio = nombreCompleto == null ? "" : nombreCompleto.trim().replaceAll("\\s+", " ");
        String[] partes = limpio.split(" ", 2);

        Operador operador = new Operador();
        operador.setNombre(partes.length > 0 ? partes[0] : "");
        operador.setApellido(partes.length > 1 ? partes[1] : "");
        operador.setDni(dni);
        operador.setCelular(celular);
        operador.setCorreo(correo);
        operador.setPassword(password);
        return operador;
    }
}
