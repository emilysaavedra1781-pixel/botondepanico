package botondepanico.controller;

import botondepanico.model.Usuario;
import botondepanico.service.EmergenciaService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class EmergenciaController {

    @Autowired
    private EmergenciaService emergenciaService;

    @PostMapping("/emergencia/activar")
    public String activarEmergencia(
            @RequestParam String tipoEmergencia,
            @RequestParam String latitud,
            @RequestParam String longitud,
            @RequestParam String ubicacion,
            HttpSession session) {

        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) return "redirect:/login";

        emergenciaService.registrarEmergencia(
            usuario, tipoEmergencia, latitud, longitud, ubicacion, null
        );

        return "redirect:/home?alerta=enviada";
    }
}