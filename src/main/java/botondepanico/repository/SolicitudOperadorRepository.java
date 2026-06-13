package botondepanico.repository;

import botondepanico.model.SolicitudOperador;
import botondepanico.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface SolicitudOperadorRepository extends JpaRepository<SolicitudOperador, Long> {

    Optional<SolicitudOperador> findByUsuario(Usuario usuario);
    boolean existsByUsuario(Usuario usuario);
}
