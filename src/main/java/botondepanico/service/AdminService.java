package botondepanico.service;

import botondepanico.model.EstadoEmergencia;
import botondepanico.model.EstadoOperador;
import botondepanico.model.SuperAdmin;
import botondepanico.repository.EmergenciaRepository;
import botondepanico.repository.OperadorRepository;
import botondepanico.repository.SuperAdminRepository;
import botondepanico.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;

@Service
public class AdminService {

    private final UsuarioRepository usuarioRepository;
    private final OperadorRepository operadorRepository;
    private final SuperAdminRepository superAdminRepository;
    private final EmergenciaRepository emergenciaRepository;

    public AdminService(UsuarioRepository usuarioRepository,
                        OperadorRepository operadorRepository,
                        SuperAdminRepository superAdminRepository,
                        EmergenciaRepository emergenciaRepository) {
        this.usuarioRepository = usuarioRepository;
        this.operadorRepository = operadorRepository;
        this.superAdminRepository = superAdminRepository;
        this.emergenciaRepository = emergenciaRepository;
    }

    public long emergenciasActivas() {
        return emergenciaRepository.countByEstadoIn(List.of(
            EstadoEmergencia.PENDIENTE,
            EstadoEmergencia.EN_ATENCION,
            EstadoEmergencia.AUTORIDAD_NOTIFICADA
        ));
    }

    public long emergenciasHoy() {
        LocalDate hoy = LocalDate.now();
        return emergenciaRepository.countByFechaBetween(hoy.atStartOfDay(), hoy.plusDays(1).atStartOfDay());
    }

    public long emergenciasMes() {
        LocalDate inicio = LocalDate.now().withDayOfMonth(1);
        return emergenciaRepository.countByFechaBetween(inicio.atStartOfDay(), inicio.plusMonths(1).atStartOfDay());
    }

    public long usuariosRegistrados() {
        return usuarioRepository.countByRol("USUARIO");
    }

    public long operadoresEnLinea() {
        return operadorRepository.countByEstadoAndEnLinea(EstadoOperador.APROBADO, true);
    }

    public long emergenciasResueltas() {
        return emergenciaRepository.countByEstado(EstadoEmergencia.RESUELTA);
    }

    public long entidadesNotificadas() {
        return emergenciaRepository.countByEstado(EstadoEmergencia.AUTORIDAD_NOTIFICADA);
    }

    public List<SuperAdmin> administradores() {
        return superAdminRepository.findAll();
    }
}
