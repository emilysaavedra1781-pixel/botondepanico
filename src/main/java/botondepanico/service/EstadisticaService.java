package botondepanico.service;

import botondepanico.model.EstadoEmergencia;
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

    public Map<String, Long> porEstado() {
        return mapear(emergenciaRepository.contarPorEstado());
    }

    public Map<String, Long> porPrioridad() {
        return mapear(emergenciaRepository.contarPorPrioridad());
    }

    public Map<String, Long> porOrigen() {
        return mapear(emergenciaRepository.contarPorOrigen());
    }

    public Map<String, Long> resumenGeneral() {
        Map<String, Long> resumen = new LinkedHashMap<>();
        resumen.put("TOTAL", emergenciaRepository.count());
        resumen.put("PENDIENTES", emergenciaRepository.countByEstado(EstadoEmergencia.PENDIENTE));
        resumen.put("EN_ATENCION", emergenciaRepository.countByEstado(EstadoEmergencia.EN_ATENCION));
        resumen.put("RESUELTAS", emergenciaRepository.countByEstado(EstadoEmergencia.RESUELTA));
        return resumen;
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
