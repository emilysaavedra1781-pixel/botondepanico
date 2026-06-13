package botondepanico.repository;

import botondepanico.model.Emergencia;
import botondepanico.model.EstadoEmergencia;
import botondepanico.model.PrioridadEmergencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmergenciaRepository extends JpaRepository<Emergencia, Long> {
    List<Emergencia> findByUsuarioId(Long usuarioId);
    List<Emergencia> findByUsuarioIdOrderByFechaDesc(Long usuarioId);
    Optional<Emergencia> findFirstByUsuarioIdAndEstadoNotOrderByFechaDesc(Long usuarioId, EstadoEmergencia estado);
    List<Emergencia> findByUsuarioIdAndEstadoOrderByFechaDesc(Long usuarioId, EstadoEmergencia estado);
    List<Emergencia> findByEstadoOrderByFechaDesc(EstadoEmergencia estado);
    List<Emergencia> findAllByOrderByFechaDesc();
    List<Emergencia> findByEstadoInOrderByFechaDesc(List<EstadoEmergencia> estados);
    List<Emergencia> findByOperadorAsignadoIdOrderByFechaDesc(Long operadorId);
    long countByEstado(EstadoEmergencia estado);
    long countByEstadoIn(List<EstadoEmergencia> estados);
    long countByFechaBetween(LocalDateTime inicio, LocalDateTime fin);
    long countByEstadoAndFechaBetween(EstadoEmergencia estado, LocalDateTime inicio, LocalDateTime fin);
    long countByOperadorAsignadoIdAndEstadoAndFechaActualizacionBetween(Long operadorId, EstadoEmergencia estado, LocalDateTime inicio, LocalDateTime fin);

    @Query("select e from Emergencia e where " +
        "(:texto is null or lower(e.tipoEmergencia) like lower(concat('%', :texto, '%')) or lower(e.direccion) like lower(concat('%', :texto, '%')) or lower(e.usuario.nombre) like lower(concat('%', :texto, '%')) or lower(e.usuario.apellido) like lower(concat('%', :texto, '%'))) and " +
        "(:estado is null or e.estado = :estado) and " +
        "(:prioridad is null or e.prioridad = :prioridad) and " +
        "(:tipo is null or lower(e.tipoEmergencia) like lower(concat('%', :tipo, '%'))) and " +
        "(:distrito is null or lower(e.distrito) like lower(concat('%', :distrito, '%'))) " +
        "order by e.fecha desc")
    List<Emergencia> buscarParaOperador(String texto, EstadoEmergencia estado, PrioridadEmergencia prioridad, String tipo, String distrito);

    @Query("select e.tipoEmergencia, count(e) from Emergencia e group by e.tipoEmergencia")
    List<Object[]> contarPorTipo();

    @Query("select coalesce(e.distrito, 'No registrado'), count(e) from Emergencia e group by coalesce(e.distrito, 'No registrado')")
    List<Object[]> contarPorDistrito();

    @Query(value = "select extract(hour from fecha) as hora, count(*) from emergencias group by extract(hour from fecha) order by extract(hour from fecha)", nativeQuery = true)
    List<Object[]> contarPorHora();

    @Query("select coalesce(e.operadorAsignado.nombre, 'Sin operador'), count(e) from Emergencia e where e.operadorAsignado is not null group by e.operadorAsignado.nombre")
    List<Object[]> contarPorOperador();
}
