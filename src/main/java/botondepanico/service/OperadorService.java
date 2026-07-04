package botondepanico.service;

import botondepanico.model.*;
import botondepanico.repository.EmergenciaRepository;
import botondepanico.repository.HistorialEmergenciaRepository;
import botondepanico.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class OperadorService {

    private final EmergenciaRepository emergenciaRepository;
    private final HistorialEmergenciaRepository historialRepository;
    private final UsuarioRepository usuarioRepository;

    public OperadorService(EmergenciaRepository emergenciaRepository,
                           HistorialEmergenciaRepository historialRepository,
                           UsuarioRepository usuarioRepository) {
        this.emergenciaRepository = emergenciaRepository;
        this.historialRepository = historialRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public List<Emergencia> pendientes() {
        return normalizar(emergenciaRepository.findByEstadoOrderByFechaDesc(EstadoEmergencia.PENDIENTE));
    }

    public List<Emergencia> activas() {
        return normalizar(emergenciaRepository.findByEstadoInOrderByFechaDesc(List.of(
            EstadoEmergencia.PENDIENTE,
            EstadoEmergencia.EN_ATENCION,
            EstadoEmergencia.AUTORIDAD_NOTIFICADA
        )));
    }

    public List<Emergencia> buscar(String texto, EstadoEmergencia estado, PrioridadEmergencia prioridad, String tipo, String distrito) {
        if (blankToNull(texto) == null && estado == null && prioridad == null && blankToNull(tipo) == null && blankToNull(distrito) == null) {
            return normalizar(emergenciaRepository.findAllByOrderByFechaDesc());
        }
        return normalizar(emergenciaRepository.buscarParaOperador(blankToNull(texto), estado, prioridad, blankToNull(tipo), blankToNull(distrito)));
    }

    public Optional<Emergencia> obtener(Long id) {
        return emergenciaRepository.findById(id).map(this::normalizar);
    }

    public Emergencia aceptar(Long id, Operador operador) {
        Emergencia emergencia = emergenciaRepository.findById(id).orElseThrow();
        emergencia.setEstado(EstadoEmergencia.EN_ATENCION);
        emergencia.setOperadorAsignado(operador);
        Emergencia guardada = emergenciaRepository.save(emergencia);
        registrarHistorial(guardada, operador, "ACEPTADA", "El operador acepto la emergencia.");
        return guardada;
    }

    public Emergencia rechazar(Long id, Operador operador, String motivo) {
        Emergencia emergencia = emergenciaRepository.findById(id).orElseThrow();
        emergencia.setEstado(EstadoEmergencia.RECHAZADA);
        emergencia.setOperadorAsignado(operador);
        emergencia.setMotivoRechazo(blankToNull(motivo));
        Emergencia guardada = emergenciaRepository.save(emergencia);
        registrarHistorial(guardada, operador, "RECHAZADA", motivo);
        return guardada;
    }

    public Emergencia clasificar(Long id, Operador operador, TipoEmergencia tipo, PrioridadEmergencia prioridad, String descripcion) {
        Emergencia emergencia = emergenciaRepository.findById(id).orElseThrow();
        emergencia.setTipoEmergencia(tipo.name());
        emergencia.setPrioridad(prioridad == null ? PrioridadEmergencia.MEDIA : prioridad);
        emergencia.setDescripcionOperador(descripcion);
        emergencia.setEstado(EstadoEmergencia.EN_ATENCION);
        emergencia.setOperadorAsignado(operador);
        Emergencia guardada = emergenciaRepository.save(emergencia);
        registrarHistorial(guardada, operador, "CLASIFICADA", descripcion);
        return guardada;
    }

    public Emergencia finalizar(Long id, Operador operador) {
        Emergencia emergencia = emergenciaRepository.findById(id).orElseThrow();
        emergencia.setEstado(EstadoEmergencia.RESUELTA);
        Emergencia guardada = emergenciaRepository.save(emergencia);
        registrarHistorial(guardada, operador, "RESUELTA", "Atencion finalizada.");
        return guardada;
    }

    public Emergencia crearReporteTelefonico(Operador operador,
                                             String nombre,
                                             String apellido,
                                             String celular,
                                             String dni,
                                             String distrito,
                                             String direccion,
                                             String tipoEmergencia,
                                             String prioridad,
                                             String descripcion) {
        Usuario usuario = usuarioRepository.findByCelular(celular)
            .orElseGet(() -> {
                Usuario nuevo = new Usuario();
                nuevo.setNombre(blankToNull(nombre));
                nuevo.setApellido(blankToNull(apellido));
                nuevo.setCelular(blankToNull(celular));
                nuevo.setDni(blankToNull(dni));
                nuevo.setDistrito(blankToNull(distrito));
                nuevo.setRol("CIUDADANO");
                nuevo.setTipoCuenta("TELEFONO");
                nuevo.setEstadoCuenta("ACTIVO");
                return usuarioRepository.save(nuevo);
            });

        Emergencia emergencia = new Emergencia();
        emergencia.setUsuario(usuario);
        emergencia.setTipoEmergencia(normalizarTipo(tipoEmergencia));
        emergencia.setEstado(EstadoEmergencia.PENDIENTE);
        emergencia.setOrigen(OrigenEmergencia.TELEFONICO);
        emergencia.setPrioridad(resolverPrioridad(prioridad));
        emergencia.setLatitud(null);
        emergencia.setLongitud(null);
        emergencia.setDistrito(blankToNull(distrito));
        emergencia.setDireccion(blankToNull(direccion));
        emergencia.setUbicacion(blankToNull(direccion));
        emergencia.setDescripcion(blankToNull(descripcion));
        emergencia.setDescripcionOperador(blankToNull(descripcion));
        Emergencia guardada = emergenciaRepository.save(emergencia);
        registrarHistorial(guardada, operador, "REPORTE_TELEFONICO", descripcion == null || descripcion.isBlank() ? "Reporte recibido por telefono" : descripcion.trim());
        return guardada;
    }

    public void registrarHistorial(Emergencia emergencia, Operador operador, String accion, String detalle) {
        HistorialEmergencia historial = new HistorialEmergencia();
        historial.setEmergencia(emergencia);
        historial.setOperador(operador);
        historial.setAccion(accion);
        historial.setDetalle(detalle);
        historialRepository.save(historial);
    }

    public long contarPendientes() {
        return emergenciaRepository.countByEstado(EstadoEmergencia.PENDIENTE);
    }

    public long contarEnAtencion() {
        return emergenciaRepository.countByEstado(EstadoEmergencia.EN_ATENCION);
    }

    public long contarAtendidasHoy(Operador operador) {
        LocalDate hoy = LocalDate.now();
        return emergenciaRepository.countByOperadorAsignadoIdAndEstadoAndFechaActualizacionBetween(
            operador.getId(),
            EstadoEmergencia.RESUELTA,
            hoy.atStartOfDay(),
            hoy.plusDays(1).atStartOfDay()
        );
    }

    public long contarTotal() {
        return emergenciaRepository.count();
    }

    public List<Emergencia> historialAtendido(Operador operador) {
        return normalizar(emergenciaRepository.findByOperadorAsignadoIdOrderByFechaDesc(operador.getId()));
    }

    private String blankToNull(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    private String normalizarTipo(String tipoEmergencia) {
        if (tipoEmergencia == null || tipoEmergencia.isBlank()) {
            return "OTRO";
        }
        return tipoEmergencia.trim().toUpperCase();
    }

    private PrioridadEmergencia resolverPrioridad(String prioridad) {
        if (prioridad == null || prioridad.isBlank()) {
            return PrioridadEmergencia.MEDIA;
        }
        try {
            return PrioridadEmergencia.valueOf(prioridad.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return PrioridadEmergencia.MEDIA;
        }
    }

    private List<Emergencia> normalizar(List<Emergencia> emergencias) {
        emergencias.forEach(this::normalizar);
        return emergencias;
    }

    private Emergencia normalizar(Emergencia emergencia) {
        if (emergencia.getPrioridad() == null) {
            emergencia.setPrioridad(PrioridadEmergencia.MEDIA);
        }
        return emergencia;
    }
}
