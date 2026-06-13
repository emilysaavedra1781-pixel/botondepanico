package botondepanico.service;

import botondepanico.model.*;
import botondepanico.repository.EntidadNotificadaRepository;
import botondepanico.repository.EmergenciaRepository;
import org.springframework.stereotype.Service;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class NotificacionService {

    private final EmergenciaRepository emergenciaRepository;
    private final EntidadNotificadaRepository entidadNotificadaRepository;
    private final CorreoService correoService;

    public NotificacionService(EmergenciaRepository emergenciaRepository,
                               EntidadNotificadaRepository entidadNotificadaRepository,
                               CorreoService correoService) {
        this.emergenciaRepository = emergenciaRepository;
        this.entidadNotificadaRepository = entidadNotificadaRepository;
        this.correoService = correoService;
    }

    public String entidadRecomendada(Emergencia emergencia) {
        String tipo = emergencia.getTipoEmergencia() == null ? "" : emergencia.getTipoEmergencia().toUpperCase();
        if (tipo.contains("MEDICA")) return "SAMU";
        if (tipo.contains("INCENDIO") || tipo.contains("FUGA")) return "Bomberos";
        return "Comisaria Distrital";
    }

    public String correoSugerido(String entidad) {
        if ("SAMU".equalsIgnoreCase(entidad)) return "samu.alertas@gmail.com";
        if ("Bomberos".equalsIgnoreCase(entidad)) return "bomberos.alertas@gmail.com";
        return "comisaria.alertas@gmail.com";
    }

    public boolean notificarAutoridad(Emergencia emergencia, Operador operador, String entidad, String correoDestino) {
        String correo = correoDestino == null || correoDestino.isBlank() ? correoSugerido(entidad) : correoDestino.trim();
        boolean correoEnviado = true;
        try {
            correoService.enviar(correo, asunto(emergencia), cuerpo(emergencia));
        } catch (RuntimeException ex) {
            correoEnviado = false;
            System.out.println("Correo no enviado, se registra notificacion simulada: " + ex.getMessage());
        }

        EntidadNotificada notificada = new EntidadNotificada();
        notificada.setEmergencia(emergencia);
        notificada.setOperador(operador);
        notificada.setEntidad(entidad);
        notificada.setCorreo(correo);
        entidadNotificadaRepository.save(notificada);

        emergencia.setEntidadNotificada(entidad);
        emergencia.setCorreoEntidadNotificada(correo);
        emergencia.setEstado(EstadoEmergencia.AUTORIDAD_NOTIFICADA);
        emergenciaRepository.save(emergencia);
        return correoEnviado;
    }

    public List<EntidadNotificada> listarPorEmergencia(Long emergenciaId) {
        return entidadNotificadaRepository.findByEmergenciaIdOrderByFechaNotificacionDesc(emergenciaId);
    }

    private String asunto(Emergencia emergencia) {
        return "ALERTA DE EMERGENCIA - " + valor(emergencia.getTipoEmergencia()) + " - " + valor(emergencia.getDistrito());
    }

    private String cuerpo(Emergencia emergencia) {
        Usuario usuario = emergencia.getUsuario();
        String fecha = emergencia.getFecha() == null ? "No registrada" : emergencia.getFecha().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        String maps = "https://www.google.com/maps?q=" + valor(emergencia.getLatitud()) + "," + valor(emergencia.getLongitud());
        return "ALERTA DE EMERGENCIA\n\n" +
            "Usuario: " + usuario.getNombre() + " " + usuario.getApellido() + "\n" +
            "Celular: " + valor(usuario.getCelular()) + "\n" +
            "DNI: " + valor(usuario.getDni()) + "\n" +
            "Fecha y hora: " + fecha + "\n" +
            "Tipo de emergencia: " + valor(emergencia.getTipoEmergencia()) + "\n" +
            "Distrito: " + valor(emergencia.getDistrito()) + "\n" +
            "Direccion: " + valor(emergencia.getDireccion()) + "\n" +
            "Ubicacion GPS: " + maps + "\n" +
            "Descripcion del operador: " + valor(emergencia.getDescripcionOperador()) + "\n" +
            "Evidencias disponibles: " + emergencia.getEvidencias().size() + "\n\n" +
            "Responder de inmediato.";
    }

    private String valor(String valor) {
        return valor == null || valor.isBlank() ? "No registrado" : valor;
    }
}
