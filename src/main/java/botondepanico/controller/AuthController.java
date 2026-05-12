package botondepanico.controller;

import botondepanico.model.Usuario;
import botondepanico.service.UsuarioService;
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