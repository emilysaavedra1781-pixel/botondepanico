package botondepanico.repository;

import botondepanico.model.EntidadNotificada;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EntidadNotificadaRepository extends JpaRepository<EntidadNotificada, Long> {
    List<EntidadNotificada> findByEmergenciaIdOrderByFechaNotificacionDesc(Long emergenciaId);
}
