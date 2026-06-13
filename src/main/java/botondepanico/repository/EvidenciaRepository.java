package botondepanico.repository;

import botondepanico.model.Evidencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EvidenciaRepository extends JpaRepository<Evidencia, Long> {
    List<Evidencia> findByEmergenciaIdOrderByFechaEnvioDesc(Long emergenciaId);
    List<Evidencia> findByUsuarioIdOrderByFechaEnvioDesc(Long usuarioId);
    long countByEmergenciaId(Long emergenciaId);
}
