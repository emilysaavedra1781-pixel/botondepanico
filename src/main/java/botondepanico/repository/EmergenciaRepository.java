package botondepanico.repository;

import botondepanico.model.Emergencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EmergenciaRepository extends JpaRepository<Emergencia, Long> {
    List<Emergencia> findByUsuarioId(Long usuarioId);
    List<Emergencia> findByUsuarioIdOrderByFechaDesc(Long usuarioId); // historial ordenado
}