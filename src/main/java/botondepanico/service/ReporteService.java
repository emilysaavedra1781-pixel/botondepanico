package botondepanico.service;

import botondepanico.model.ReporteEmergencia;
import botondepanico.model.SuperAdmin;
import botondepanico.repository.ReporteEmergenciaRepository;
import org.springframework.stereotype.Service;

@Service
public class ReporteService {

    private final ReporteEmergenciaRepository repository;

    public ReporteService(ReporteEmergenciaRepository repository) {
        this.repository = repository;
    }

    public ReporteEmergencia registrar(String periodo, String distrito, String tipo, SuperAdmin admin) {
        ReporteEmergencia reporte = new ReporteEmergencia();
        reporte.setPeriodo(periodo);
        reporte.setDistrito(distrito);
        reporte.setTipo(tipo);
        reporte.setGeneradoPor(admin);
        return repository.save(reporte);
    }
}
