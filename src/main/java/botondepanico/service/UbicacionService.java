package botondepanico.service;

import botondepanico.model.Ubicacion;
import botondepanico.repository.UbicacionRepository;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class UbicacionService {

    private final UbicacionRepository ubicacionRepository;

    public UbicacionService(UbicacionRepository ubicacionRepository) {
        this.ubicacionRepository = ubicacionRepository;
    }

    public Optional<Ubicacion> obtenerPorEmergencia(Long emergenciaId) {
        return ubicacionRepository.findByEmergenciaId(emergenciaId);
    }
}
