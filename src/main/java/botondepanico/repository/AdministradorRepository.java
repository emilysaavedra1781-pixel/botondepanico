package botondepanico.repository;

import botondepanico.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AdministradorRepository extends JpaRepository<Usuario, Long> {
    List<Usuario> findByRolInOrderByIdAsc(List<String> roles);
}
