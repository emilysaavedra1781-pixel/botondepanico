package botondepanico.service;

import botondepanico.model.Emergencia;
import botondepanico.model.Usuario;
import botondepanico.repository.EmergenciaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EmergenciaService {

    @Autowired
    private EmergenciaRepository emergenciaRepository;

    @Autowired
    private EmailService emailService;

    public void registrarEmergencia(Usuario usuario, String tipoEmergencia,
                                     String latitud, String longitud,
                                     String ubicacion, String fotoVideo) {

        // 🔍 DEBUG 1: qué tipo llega del frontend
        System.out.println("TIPO RECIBIDO: " + tipoEmergencia);

        // 1. Guardar en BD
        Emergencia emergencia = new Emergencia();
        emergencia.setUsuario(usuario);
        emergencia.setTipoEmergencia(tipoEmergencia);
        emergencia.setLatitud(latitud);
        emergencia.setLongitud(longitud);
        emergencia.setUbicacion(ubicacion);
        emergencia.setFotoVideo(fotoVideo);
        emergenciaRepository.save(emergencia);

        // 2. Obtener correo según tipo
        String correoAutoridad = obtenerCorreo(tipoEmergencia);

        // 🔍 DEBUG 2: ver correo seleccionado
        System.out.println("CORREO AUTORIDAD: " + correoAutoridad);

        try {
            emailService.enviarAlerta(
                correoAutoridad,
                usuario.getNombre() + " " + usuario.getApellido(),
                usuario.getCelular(),
                tipoEmergencia,
                latitud,
                longitud
            );

            System.out.println("✅ Correo enviado a: " + correoAutoridad);

        } catch (Exception e) {
            System.out.println("❌ Error enviando correo: " + e.getMessage());
        }
    }

    private String obtenerCorreo(String tipo) {

        tipo = tipo.trim().toUpperCase();

        return switch (tipo) {
            case "MEDICA"     -> "emilysaacedra200417@gmail.com";
            case "INCENDIO"   -> "camilabr0502@gmail.com";
            case "SEGURIDAD"  -> "alonsocisnerosilz@gmail.com";
            case "COMISARIA"  -> "emilysaacedra200417@gmail.com";
            default           -> "emilysaavedra.17.8.1@gmail.com";
        };
    }
}