package botondepanico.service;

import botondepanico.model.Evidencia;
import botondepanico.repository.EvidenciaRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class EvidenciaService {

    private final EvidenciaRepository evidenciaRepository;

    public EvidenciaService(EvidenciaRepository evidenciaRepository) {
        this.evidenciaRepository = evidenciaRepository;
    }

    public List<Evidencia> listarPorEmergencia(Long emergenciaId) {
        return evidenciaRepository.findByEmergenciaIdOrderByFechaEnvioDesc(emergenciaId);
    }
}
