package botondepanico.controller;

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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @PostMapping("/login")
    public String procesarLogin(@RequestParam String correo,
                                @RequestParam String contrasena,
                                HttpSession session,
                                Model model) {
        Usuario usuario = usuarioService.loginPorCorreo(correo, contrasena);
        if (usuario != null && "USUARIO".equalsIgnoreCase(usuario.getRol())) {
            if ("BLOQUEADO".equalsIgnoreCase(usuario.getEstadoCuenta())) {
                model.addAttribute("error", "Tu cuenta no se encuentra habilitada para acceder");
                return "login";
            }
            session.setAttribute("usuario", usuario);
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
            session.setAttribute("operador", operador);
            return "redirect:/operador/dashboard";
        }

        SuperAdmin admin = usuarioService.loginSuperAdminPorCorreo(correo, contrasena);
        if (admin != null) {
            if (admin.getEstado() == EstadoUsuario.BLOQUEADO || admin.getEstado() == EstadoUsuario.INACTIVO) {
                model.addAttribute("error", "Tu cuenta no se encuentra habilitada para acceder");
                return "login";
            }
            session.setAttribute("admin", admin);
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
    public String registroUsuario() {
        return "registro-usuario";
    }

    @PostMapping("/registro-usuario")
    public String registrarUsuario(@RequestParam String nombreCompleto,
                                   @RequestParam String dni,
                                   @RequestParam String celular,
                                   @RequestParam String distrito,
                                   @RequestParam String correo,
                                   @RequestParam String password,
                                   @RequestParam String confirmarPassword,
                                   HttpSession session,
                                   Model model) {
        if (!password.equals(confirmarPassword)) {
            model.addAttribute("error", "Las contrasenas no coinciden");
            return "registro-usuario";
        }

        Usuario usuario = crearUsuarioBase(nombreCompleto, dni, celular, correo, password);
        usuario.setDistrito(distrito);

        Usuario guardado = usuarioService.registrarUsuario(usuario);
        if (guardado == null) {
            model.addAttribute("error", "El celular, DNI o correo ya esta registrado");
            return "registro-usuario";
        }

        session.setAttribute("usuario", guardado);
        return "redirect:/usuario/dashboard";
    }

    @GetMapping("/registro-operador")
    public String registroOperador() {
        return "registro-operador";
    }

    @PostMapping("/registro-operador")
    public String registrarOperador(@RequestParam String nombreCompleto,
                                    @RequestParam String dni,
                                    @RequestParam String celular,
                                    @RequestParam String correo,
                                    @RequestParam String password,
                                    @RequestParam String confirmarPassword,
                                    Model model) {
        if (!password.equals(confirmarPassword)) {
            model.addAttribute("error", "Las contrasenas no coinciden");
            return "registro-operador";
        }

        Operador operador = crearOperadorBase(nombreCompleto, dni, celular, correo, password);

        Operador guardado = usuarioService.registrarOperadorPendiente(operador);
        if (guardado == null) {
            model.addAttribute("error", "El celular, DNI o correo ya esta registrado");
            return "registro-operador";
        }

        return "redirect:/operador-pendiente";
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
