package botondepanico.controller;

import botondepanico.model.Usuario;
import botondepanico.service.UsuarioService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    // ✅ AGREGADO — procesa login con HttpSession
    @PostMapping("/login")
    public String procesarLogin(@RequestParam String celular,
                                @RequestParam String contrasena,
                                HttpSession session,
                                Model model) {
        Usuario usuario = usuarioService.login(celular, contrasena);
        if (usuario != null) {
            session.setAttribute("usuario", usuario);
            return "redirect:/home";
        }
        model.addAttribute("error", "Celular o contraseña incorrectos");
        return "login";
    }

    // ✅ AGREGADO — cierra sesión
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    @GetMapping("/registro")
    public String registro(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "registro";
    }

    @PostMapping("/registro")
    public String registrar(@ModelAttribute Usuario usuario, Model model) {
        boolean exito = usuarioService.registrar(usuario);
        if (exito) {
            return "redirect:/login";
        } else {
            model.addAttribute("error", "El celular, DNI o correo ya está registrado");
            return "registro";
        }
    }
}