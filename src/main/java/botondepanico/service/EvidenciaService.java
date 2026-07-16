package botondepanico.service;

import botondepanico.model.Evidencia;
import botondepanico.repository.EvidenciaRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class EvidenciaService {

    private final EvidenciaRepository evidenciaRepository;

    public EvidenciaService(EvidenciaRepository evidenciaRepository) {
        this.evidenciaRepository = evidenciaRepository;
    }

    public List<Evidencia> listarPorEmergencia(Long emergenciaId) {
        return evidenciaRepository.findByEmergenciaIdOrderByFechaEnvioDesc(emergenciaId);
    }

    public Evidencia guardar(Evidencia evidencia) {
        return evidenciaRepository.save(evidencia);
    }

    public Optional<Evidencia> obtener(Long id) {
        return evidenciaRepository.findById(id);
    }
}
