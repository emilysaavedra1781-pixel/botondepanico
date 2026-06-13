package botondepanico.service;

import botondepanico.model.Emergencia;
import botondepanico.model.EstadoEmergencia;
import botondepanico.model.PrioridadEmergencia;
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

        Emergencia emergencia = new Emergencia();
        emergencia.setUsuario(usuario);
        emergencia.setTipoEmergencia(tipoEmergencia);
        emergencia.setLatitud(latitud);
        emergencia.setLongitud(longitud);
        emergencia.setUbicacion(ubicacion);
        emergencia.setEstado(EstadoEmergencia.PENDIENTE);
        emergencia.setPrioridad(PrioridadEmergencia.ALTA);
        emergencia.setFotoVideo(fotoVideo);
        emergenciaRepository.save(emergencia);

        String correoAutoridad = obtenerCorreo(tipoEmergencia);

        try {
            emailService.enviarAlerta(
                correoAutoridad,
                usuario.getNombre() + " " + usuario.getApellido(),
                usuario.getCelular(),
                tipoEmergencia,
                latitud,
                longitud
            );

        } catch (Exception e) {
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
