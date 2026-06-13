package botondepanico.service;

import botondepanico.model.Emergencia;
import botondepanico.model.EstadoEmergencia;
import botondepanico.model.Evidencia;
import botondepanico.model.Operador;
import botondepanico.model.PrioridadEmergencia;
import botondepanico.model.SuperAdmin;
import botondepanico.model.Ubicacion;
import botondepanico.model.Usuario;
import botondepanico.repository.EmergenciaRepository;
import botondepanico.repository.EvidenciaRepository;
import botondepanico.repository.UbicacionRepository;
import botondepanico.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UsuarioModuloService {

    private static final Path EVIDENCIAS_DIR = Paths.get("uploads", "evidencias");

    @Autowired
    private EmergenciaRepository emergenciaRepository;

    @Autowired
    private EvidenciaRepository evidenciaRepository;

    @Autowired
    private UbicacionRepository ubicacionRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private OperadorAlertaWebSocketService operadorAlertaWebSocketService;

    public Emergencia activarSos(Usuario usuario, String latitud, String longitud,
                                 String distrito, String direccion, String tipoEmergencia) {
        Emergencia emergencia = new Emergencia();
        emergencia.setUsuario(usuario);
        emergencia.setTipoEmergencia(normalizarTipo(tipoEmergencia));
        emergencia.setEstado(EstadoEmergencia.PENDIENTE);
        emergencia.setPrioridad(PrioridadEmergencia.ALTA);
        emergencia.setLatitud(latitud);
        emergencia.setLongitud(longitud);
        emergencia.setDistrito(distrito);
        emergencia.setDireccion(direccion);
        emergencia.setUbicacion(direccion);
        emergencia.setDescripcion("Emergencia generada desde boton SOS.");

        Emergencia guardada = emergenciaRepository.save(emergencia);

        Ubicacion ubicacion = new Ubicacion();
        ubicacion.setEmergencia(guardada);
        ubicacion.setLatitud(latitud);
        ubicacion.setLongitud(longitud);
        ubicacion.setDistrito(distrito);
        ubicacion.setDireccion(direccion);
        ubicacionRepository.save(ubicacion);

        operadorAlertaWebSocketService.broadcastNuevaEmergencia(guardada);

        return guardada;
    }

    public Optional<Emergencia> obtenerEmergenciaActiva(Usuario usuario) {
        return emergenciaRepository.findFirstByUsuarioIdAndEstadoNotOrderByFechaDesc(
            usuario.getId(),
            EstadoEmergencia.RESUELTA
        );
    }

    public Optional<Emergencia> obtenerUltimaEmergencia(Usuario usuario) {
        return emergenciaRepository.findByUsuarioIdOrderByFechaDesc(usuario.getId()).stream().findFirst();
    }

    public List<Emergencia> listarHistorial(Usuario usuario) {
        return emergenciaRepository.findByUsuarioIdOrderByFechaDesc(usuario.getId());
    }

    public Optional<Emergencia> obtenerEmergenciaDeUsuario(Long emergenciaId, Usuario usuario) {
        return emergenciaRepository.findById(emergenciaId)
            .filter(emergencia -> emergencia.getUsuario().getId().equals(usuario.getId()));
    }

    public Optional<Evidencia> obtenerEvidenciaDeUsuario(Long evidenciaId, Usuario usuario) {
        return evidenciaRepository.findById(evidenciaId)
            .filter(evidencia -> evidencia.getUsuario().getId().equals(usuario.getId()));
    }

    public Optional<Evidencia> obtenerEvidenciaVisible(Long evidenciaId, Usuario usuario) {
        return evidenciaRepository.findById(evidenciaId)
            .filter(evidencia ->
                evidencia.getUsuario().getId().equals(usuario.getId())
            );
    }

    public Optional<Evidencia> obtenerEvidenciaVisible(Long evidenciaId, Operador operador) {
        return evidenciaRepository.findById(evidenciaId)
            .filter(evidencia ->
                evidencia.getEmergencia().getOperadorAsignado() != null &&
                evidencia.getEmergencia().getOperadorAsignado().getId().equals(operador.getId())
            );
    }

    public Optional<Evidencia> obtenerEvidenciaVisible(Long evidenciaId, SuperAdmin admin) {
        return evidenciaRepository.findById(evidenciaId);
    }

    public List<Evidencia> listarEvidenciasUsuario(Usuario usuario) {
        return evidenciaRepository.findByUsuarioIdOrderByFechaEnvioDesc(usuario.getId());
    }

    public Evidencia guardarEvidencia(Usuario usuario, MultipartFile archivo, String tipo,
                                      String latitud, String longitud, String direccion,
                                      String descripcion) throws IOException {
        return guardarEvidencia(usuario, archivo, tipo, latitud, longitud, direccion, descripcion, null, false);
    }

    public Evidencia guardarEvidencia(Usuario usuario, MultipartFile archivo, String tipo,
                                      String latitud, String longitud, String direccion,
                                      String descripcion, Long emergenciaId, boolean nuevoCaso) throws IOException {
        if (archivo == null || archivo.isEmpty()) {
            throw new IllegalArgumentException("Selecciona un archivo para enviar");
        }

        Emergencia emergencia = resolverEmergenciaParaEvidencia(
            usuario,
            latitud,
            longitud,
            direccion,
            tipo,
            emergenciaId,
            nuevoCaso
        );

        Files.createDirectories(EVIDENCIAS_DIR);

        String original = archivo.getOriginalFilename() == null ? "evidencia" : archivo.getOriginalFilename();
        String extension = obtenerExtension(original, archivo.getContentType());
        long numero = evidenciaRepository.countByEmergenciaId(emergencia.getId()) + 1;
        String nombreArchivo = "evidencia" + numero + "_" + ubicacionParaNombre(emergencia) + extension;
        String nombreSeguro = UUID.randomUUID() + "_" + nombreArchivo;
        Path destino = EVIDENCIAS_DIR.resolve(nombreSeguro).toAbsolutePath().normalize();

        Files.copy(archivo.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);

        Evidencia evidencia = new Evidencia();
        evidencia.setEmergencia(emergencia);
        evidencia.setUsuario(usuario);
        evidencia.setTipo(tipo == null || tipo.isBlank() ? detectarTipo(archivo.getContentType()) : tipo);
        evidencia.setNombreArchivo(nombreArchivo);
        evidencia.setRutaArchivo(destino.toString());
        evidencia.setContentType(archivo.getContentType());
        evidencia.setTamanoBytes(archivo.getSize());
        evidencia.setDescripcion(valorSeguro(descripcion, null));
        evidencia.setFechaEnvio(LocalDateTime.now());

        if (descripcion != null && !descripcion.isBlank()) {
            emergencia.setDescripcion(descripcion.trim());
            emergenciaRepository.save(emergencia);
        }

        return evidenciaRepository.save(evidencia);
    }

    private Emergencia resolverEmergenciaParaEvidencia(Usuario usuario, String latitud, String longitud,
                                                       String direccion, String tipo, Long emergenciaId,
                                                       boolean nuevoCaso) {
        if (emergenciaId != null) {
            return emergenciaRepository.findById(emergenciaId)
                .filter(emergencia -> emergencia.getUsuario().getId().equals(usuario.getId()))
                .orElseThrow(() -> new IllegalArgumentException("No se encontro el caso para adjuntar la evidencia"));
        }

        if (nuevoCaso) {
            return activarSos(
                usuario,
                valorSeguro(latitud, "0"),
                valorSeguro(longitud, "0"),
                null,
                valorSeguro(direccion, "Ubicacion no registrada"),
                tipo == null || tipo.isBlank() ? "OTRO" : tipo
            );
        }

        return obtenerEmergenciaActiva(usuario)
            .orElseGet(() -> activarSos(
                usuario,
                valorSeguro(latitud, "0"),
                valorSeguro(longitud, "0"),
                null,
                valorSeguro(direccion, "Ubicacion no registrada"),
                tipo == null || tipo.isBlank() ? "OTRO" : tipo
            ));
    }

    private String valorSeguro(String valor, String defecto) {
        return valor == null || valor.isBlank() ? defecto : valor;
    }

    private String obtenerExtension(String original, String contentType) {
        int punto = original.lastIndexOf('.');
        if (punto >= 0 && punto < original.length() - 1) {
            return original.substring(punto).toLowerCase();
        }
        if (contentType == null) {
            return ".bin";
        }
        if (contentType.startsWith("image/")) {
            return ".jpg";
        }
        if (contentType.startsWith("video/")) {
            return ".webm";
        }
        if (contentType.startsWith("audio/")) {
            return ".webm";
        }
        return ".bin";
    }

    private String ubicacionParaNombre(Emergencia emergencia) {
        String ubicacion = primerTexto(
            emergencia.getDireccion(),
            emergencia.getUbicacion(),
            emergencia.getDistrito(),
            coordenadas(emergencia)
        );

        String limpio = ubicacion.toLowerCase()
            .replaceAll("[^a-z0-9]+", "_")
            .replaceAll("_+", "_")
            .replaceAll("^_|_$", "");

        return limpio.isBlank() ? "ubicacion_no_registrada" : limpio;
    }

    private String primerTexto(String... valores) {
        for (String valor : valores) {
            if (valor != null && !valor.isBlank()) {
                return valor;
            }
        }
        return "ubicacion_no_registrada";
    }

    private String coordenadas(Emergencia emergencia) {
        if (emergencia.getLatitud() == null || emergencia.getLongitud() == null) {
            return "ubicacion_no_registrada";
        }
        return emergencia.getLatitud() + "_" + emergencia.getLongitud();
    }

    private String normalizarTipo(String tipoEmergencia) {
        if (tipoEmergencia == null || tipoEmergencia.isBlank()) {
            return "OTRO";
        }
        return tipoEmergencia.trim().toUpperCase();
    }

    private String detectarTipo(String contentType) {
        if (contentType == null) {
            return "ARCHIVO";
        }
        if (contentType.startsWith("image/")) {
            return "FOTO";
        }
        if (contentType.startsWith("video/")) {
            return "VIDEO";
        }
        if (contentType.startsWith("audio/")) {
            return "AUDIO";
        }
        return "ARCHIVO";
    }
}
