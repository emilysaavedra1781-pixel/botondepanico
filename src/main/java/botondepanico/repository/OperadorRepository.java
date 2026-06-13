package botondepanico.repository;

import botondepanico.model.EstadoOperador;
import botondepanico.model.Operador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface OperadorRepository extends JpaRepository<Operador, Long> {
    Optional<Operador> findByCorreo(String correo);
    boolean existsByCorreo(String correo);
    boolean existsByCelular(String celular);
    boolean existsByDni(String dni);
    List<Operador> findByEstadoOrderByFechaRegistroDesc(EstadoOperador estado);
    List<Operador> findAllByOrderByFechaRegistroDesc();
    long countByEstado(EstadoOperador estado);
    long countByEstadoAndEnLinea(EstadoOperador estado, boolean enLinea);
}
