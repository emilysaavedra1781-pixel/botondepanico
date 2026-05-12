package botondepanico.repository;

import botondepanico.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByCelular(String celular);
    boolean existsByCelular(String celular);
    boolean existsByDni(String dni);
    boolean existsByCorreo(String correo);
}