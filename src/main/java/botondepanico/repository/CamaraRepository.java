package botondepanico.repository;

import botondepanico.model.Camara;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CamaraRepository extends JpaRepository<Camara, Long> {
    List<Camara> findByActivaTrueOrderByNombreAsc();
    List<Camara> findAllByOrderByNombreAsc();
}
