package botondepanico.repository;

import botondepanico.model.HistorialEmergencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface HistorialEmergenciaRepository extends JpaRepository<HistorialEmergencia, Long> {
    List<HistorialEmergencia> findByEmergenciaIdOrderByFechaDesc(Long emergenciaId);
    List<HistorialEmergencia> findByOperadorIdOrderByFechaDesc(Long operadorId);
}
