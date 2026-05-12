package botondepanico.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import botondepanico.model.Usuario;
import botondepanico.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;

@Controller
public class HomeController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping("/home")
    public String home(Authentication authentication, Model model) {
        String celular = authentication.getName();
        usuarioService.buscarPorCelular(celular).ifPresent(usuario -> {
            model.addAttribute("nombre", usuario.getNombre());
            model.addAttribute("apellido", usuario.getApellido());
        });
        return "home";
    }
}