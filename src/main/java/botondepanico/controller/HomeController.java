package botondepanico.controller;

import botondepanico.model.Usuario;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String index() {
        return "redirect:/login";
    }

    @GetMapping("/home")
    public String home(HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) return "redirect:/login";

        model.addAttribute("nombre", usuario.getNombre());
        model.addAttribute("apellido", usuario.getApellido());
        return "home";
    }

    @GetMapping("/camara")
    public String camara(HttpSession session) {
        if (session.getAttribute("usuario") == null) return "redirect:/login";
        return "camara";
    }

    @GetMapping("/notificaciones")
    public String notificaciones(HttpSession session) {
        if (session.getAttribute("usuario") == null) return "redirect:/login";
        return "notificaciones";
    }
}
