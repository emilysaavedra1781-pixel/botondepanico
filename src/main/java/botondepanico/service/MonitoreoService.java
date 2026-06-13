package botondepanico.service;

import botondepanico.model.Emergencia;
import botondepanico.model.EstadoEmergencia;
import botondepanico.model.EstadoOperador;
import botondepanico.model.Operador;
import botondepanico.repository.EmergenciaRepository;
import botondepanico.repository.OperadorRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class MonitoreoService {

    private final EmergenciaRepository emergenciaRepository;
    private final OperadorRepository operadorRepository;

    public MonitoreoService(EmergenciaRepository emergenciaRepository, OperadorRepository operadorRepository) {
        this.emergenciaRepository = emergenciaRepository;
        this.operadorRepository = operadorRepository;
    }

    public List<Emergencia> emergenciasActivas() {
        return emergenciaRepository.findByEstadoInOrderByFechaDesc(List.of(
            EstadoEmergencia.PENDIENTE,
            EstadoEmergencia.EN_ATENCION,
            EstadoEmergencia.AUTORIDAD_NOTIFICADA
        ));
    }

    public List<Operador> operadoresActivos() {
        return operadorRepository.findByEstadoOrderByFechaRegistroDesc(EstadoOperador.APROBADO);
    }
}
