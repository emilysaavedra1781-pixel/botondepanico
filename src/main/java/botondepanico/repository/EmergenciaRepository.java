package botondepanico.repository;

import botondepanico.dto.EmergenciaResumenDTO;
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
    Optional<Emergencia> findFirstByUsuarioIdOrderByFechaDesc(Long usuarioId);
    Optional<Emergencia> findFirstByUsuarioIdAndEstadoNotOrderByFechaDesc(Long usuarioId, EstadoEmergencia estado);
    List<Emergencia> findByUsuarioIdAndEstadoOrderByFechaDesc(Long usuarioId, EstadoEmergencia estado);
    List<Emergencia> findByEstadoOrderByFechaDesc(EstadoEmergencia estado);
    List<Emergencia> findAllByOrderByFechaDesc();
    List<Emergencia> findByEstadoInOrderByFechaDesc(List<EstadoEmergencia> estados);
    List<Emergencia> findByOperadorAsignadoIdOrderByFechaDesc(Long operadorId);

    @Query("SELECT new botondepanico.dto.EmergenciaResumenDTO(e.id, e.tipoEmergencia, e.estado, e.prioridad, e.distrito, e.direccion, e.latitud, e.longitud, e.fecha, e.usuario.id, e.usuario.nombre, e.usuario.apellido, e.origen, e.operadorAsignado.nombre, e.entidadNotificada) " +
           "FROM Emergencia e " +
           "WHERE e.estado = :estado " +
           "ORDER BY e.fecha DESC")
    List<EmergenciaResumenDTO> listarResumenPorEstado(EstadoEmergencia estado);

    @Query("SELECT new botondepanico.dto.EmergenciaResumenDTO(e.id, e.tipoEmergencia, e.estado, e.prioridad, e.distrito, e.direccion, e.latitud, e.longitud, e.fecha, e.usuario.id, e.usuario.nombre, e.usuario.apellido, e.origen, e.operadorAsignado.nombre, e.entidadNotificada) " +
           "FROM Emergencia e " +
           "WHERE e.estado IN :estados " +
           "ORDER BY e.fecha DESC")
    List<EmergenciaResumenDTO> listarResumenPorEstados(List<EstadoEmergencia> estados);

    @Query("SELECT new botondepanico.dto.EmergenciaResumenDTO(e.id, e.tipoEmergencia, e.estado, e.prioridad, e.distrito, e.direccion, e.latitud, e.longitud, e.fecha, e.usuario.id, e.usuario.nombre, e.usuario.apellido, e.origen, e.operadorAsignado.nombre, e.entidadNotificada) " +
           "FROM Emergencia e " +
           "WHERE (:texto is null or " +
           "  lower(cast(e.tipoEmergencia as string)) like lower(concat('%', cast(:texto as string), '%')) or " +
           "  lower(cast(e.direccion as string))      like lower(concat('%', cast(:texto as string), '%')) or " +
           "  lower(cast(e.usuario.nombre as string)) like lower(concat('%', cast(:texto as string), '%')) or " +
           "  lower(cast(e.usuario.apellido as string)) like lower(concat('%', cast(:texto as string), '%'))) and " +
           "(:estado is null or e.estado = :estado) and " +
           "(:prioridad is null or e.prioridad = :prioridad) and " +
           "(:tipo is null or lower(cast(e.tipoEmergencia as string)) like lower(concat('%', cast(:tipo as string), '%'))) and " +
           "(:distrito is null or lower(cast(e.distrito as string)) like lower(concat('%', cast(:distrito as string), '%'))) " +
           "ORDER BY e.fecha DESC")
    List<EmergenciaResumenDTO> buscarResumenParaOperador(String texto, EstadoEmergencia estado, PrioridadEmergencia prioridad, String tipo, String distrito);

    @Query("SELECT new botondepanico.dto.EmergenciaResumenDTO(e.id, e.tipoEmergencia, e.estado, e.prioridad, e.distrito, e.direccion, e.latitud, e.longitud, e.fecha, e.usuario.id, e.usuario.nombre, e.usuario.apellido, e.origen, e.operadorAsignado.nombre, e.entidadNotificada) " +
           "FROM Emergencia e " +
           "WHERE e.operadorAsignado.id = :operadorId " +
           "ORDER BY e.fecha DESC")
    List<EmergenciaResumenDTO> listarHistorialResumenPorOperador(Long operadorId);
    long countByEstado(EstadoEmergencia estado);
    long countByEstadoIn(List<EstadoEmergencia> estados);
    long countByFechaBetween(LocalDateTime inicio, LocalDateTime fin);
    long countByEstadoAndFechaBetween(EstadoEmergencia estado, LocalDateTime inicio, LocalDateTime fin);
    long countByOperadorAsignadoIdAndEstadoAndFechaActualizacionBetween(Long operadorId, EstadoEmergencia estado, LocalDateTime inicio, LocalDateTime fin);

    @Query("select e from Emergencia e where " +
        "(:texto is null or " +
        "  lower(cast(e.tipoEmergencia as string)) like lower(concat('%', cast(:texto as string), '%')) or " +
        "  lower(cast(e.direccion as string))      like lower(concat('%', cast(:texto as string), '%')) or " +
        "  lower(cast(e.usuario.nombre as string)) like lower(concat('%', cast(:texto as string), '%')) or " +
        "  lower(cast(e.usuario.apellido as string)) like lower(concat('%', cast(:texto as string), '%'))) and " +
        "(:estado is null or e.estado = :estado) and " +
        "(:prioridad is null or e.prioridad = :prioridad) and " +
        "(:tipo is null or lower(cast(e.tipoEmergencia as string)) like lower(concat('%', cast(:tipo as string), '%'))) and " +
        "(:distrito is null or lower(cast(e.distrito as string)) like lower(concat('%', cast(:distrito as string), '%'))) " +
        "order by e.fecha desc")
    List<Emergencia> buscarParaOperador(String texto, EstadoEmergencia estado, PrioridadEmergencia prioridad, String tipo, String distrito);

    @Query("select e.tipoEmergencia, count(e) from Emergencia e group by e.tipoEmergencia")
    List<Object[]> contarPorTipo();

    @Query("select coalesce(e.distrito, 'No registrado'), count(e) from Emergencia e group by coalesce(e.distrito, 'No registrado')")
    List<Object[]> contarPorDistrito();

    @Query(value = "select extract(hour from fecha) as hora, count(*) from emergencias group by extract(hour from fecha) order by extract(hour from fecha)", nativeQuery = true)
    List<Object[]> contarPorHora();

    @Query("select e.estado, count(e) from Emergencia e group by e.estado")
    List<Object[]> contarPorEstado();

    @Query("select e.prioridad, count(e) from Emergencia e group by e.prioridad")
    List<Object[]> contarPorPrioridad();

    @Query("select e.origen, count(e) from Emergencia e group by e.origen")
    List<Object[]> contarPorOrigen();

    @Query("select coalesce(e.operadorAsignado.nombre, 'Sin operador'), count(e) from Emergencia e where e.operadorAsignado is not null group by e.operadorAsignado.nombre")
    List<Object[]> contarPorOperador();
}
