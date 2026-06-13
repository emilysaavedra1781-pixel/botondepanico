package botondepanico.repository;

import botondepanico.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByCelular(String celular);
    Optional<Usuario> findByCorreo(String correo);
    Optional<Usuario> findFirstByRolAndEstadoCuentaOrderByIdAsc(String rol, String estadoCuenta);
    List<Usuario> findByRolAndEstadoCuentaOrderByIdAsc(String rol, String estadoCuenta);
    List<Usuario> findByRolOrderByFechaRegistroDesc(String rol);
    List<Usuario> findByRolInOrderByFechaRegistroDesc(List<String> roles);
    long countByRol(String rol);
    long countByRolAndEstadoCuenta(String rol, String estadoCuenta);
    boolean existsByCelular(String celular);
    boolean existsByDni(String dni);
    boolean existsByCorreo(String correo);
}
