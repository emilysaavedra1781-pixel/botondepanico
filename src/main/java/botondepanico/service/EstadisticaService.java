package botondepanico.service;

import botondepanico.repository.EmergenciaRepository;
import org.springframework.stereotype.Service;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class EstadisticaService {

    private final EmergenciaRepository emergenciaRepository;

    public EstadisticaService(EmergenciaRepository emergenciaRepository) {
        this.emergenciaRepository = emergenciaRepository;
    }

    public Map<String, Long> porTipo() {
        return mapear(emergenciaRepository.contarPorTipo());
    }

    public Map<String, Long> porDistrito() {
        return mapear(emergenciaRepository.contarPorDistrito());
    }

    public Map<String, Long> porHora() {
        return mapear(emergenciaRepository.contarPorHora());
    }

    public Map<String, Long> porOperador() {
        return mapear(emergenciaRepository.contarPorOperador());
    }

    private Map<String, Long> mapear(java.util.List<Object[]> filas) {
        Map<String, Long> datos = new LinkedHashMap<>();
        for (Object[] fila : filas) {
            datos.put(String.valueOf(fila[0]), ((Number) fila[1]).longValue());
        }
        return datos;
    }
}
