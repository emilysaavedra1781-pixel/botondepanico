package botondepanico.config;

import botondepanico.model.EstadoOperador;
import botondepanico.model.EstadoUsuario;
import botondepanico.model.Operador;
import botondepanico.model.SuperAdmin;
import botondepanico.model.Usuario;
import botondepanico.repository.OperadorRepository;
import botondepanico.repository.SuperAdminRepository;
import botondepanico.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class AdminDataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final OperadorRepository operadorRepository;
    private final SuperAdminRepository superAdminRepository;
    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;

    public AdminDataInitializer(UsuarioRepository usuarioRepository,
                                OperadorRepository operadorRepository,
                                SuperAdminRepository superAdminRepository,
                                JdbcTemplate jdbcTemplate,
                                PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.operadorRepository = operadorRepository;
        this.superAdminRepository = superAdminRepository;
        this.jdbcTemplate = jdbcTemplate;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        prepararTablasSeparadas();
        sincronizarSuperAdminInicial();
        usuarioRepository.findByRolInOrderByFechaRegistroDesc(List.of("OPERADOR", "OPERADOR_PENDIENTE"))
            .forEach(this::sincronizarOperador);
        limpiarFilasLegacyVacias();
        limpiarCuentasNoCiudadanasDeUsuarios();
    }

    private void prepararTablasSeparadas() {
        ejecutarSqlSeguro("alter table operadores alter column usuario_id drop not null");
        ejecutarSqlSeguro("alter table superadmins alter column usuario_id drop not null");
    }

    private void sincronizarSuperAdminInicial() {
        if (superAdminRepository.existsByCorreo("admin@botonpanico.com")) {
            return;
        }
        SuperAdmin superAdmin = new SuperAdmin();
        superAdmin.setNombre("Superadmin");
        superAdmin.setApellido("Sistema");
        superAdmin.setCorreo("admin@botonpanico.com");
        superAdmin.setDni("00000000");
        superAdmin.setCelular("999999999");
        superAdmin.setPassword(passwordEncoder.encode("admin123"));
        superAdmin.setEstado(EstadoUsuario.ACTIVO);
        superAdminRepository.save(superAdmin);
    }

    private void sincronizarOperador(Usuario usuario) {
        if (operadorRepository.existsByCorreo(usuario.getCorreo())) {
            return;
        }
        Operador operador = new Operador();
        operador.setNombre(usuario.getNombre());
        operador.setApellido(usuario.getApellido());
        operador.setCorreo(usuario.getCorreo());
        operador.setCelular(usuario.getCelular());
        operador.setDni(usuario.getDni());
        operador.setDistrito(usuario.getDistrito());
        operador.setPassword(usuario.getPassword());
        operador.setFechaRegistro(usuario.getFechaRegistro());
        operador.setUltimoAcceso(usuario.getUltimoAcceso());
        if ("PENDIENTE".equalsIgnoreCase(usuario.getEstadoCuenta()) ||
            "OPERADOR_PENDIENTE".equalsIgnoreCase(usuario.getRol())) {
            operador.setEstado(EstadoOperador.PENDIENTE);
        } else if ("RECHAZADO".equalsIgnoreCase(usuario.getEstadoCuenta())) {
            operador.setEstado(EstadoOperador.RECHAZADO);
        } else if ("BLOQUEADO".equalsIgnoreCase(usuario.getEstadoCuenta())) {
            operador.setEstado(EstadoOperador.BLOQUEADO);
        } else {
            operador.setEstado(EstadoOperador.APROBADO);
            if (operador.getFechaAprobacion() == null) {
                operador.setFechaAprobacion(LocalDateTime.now());
            }
        }
        operador.setEnLinea("ACTIVO".equalsIgnoreCase(usuario.getEstadoCuenta()));
        Operador guardado = operadorRepository.save(operador);
        ejecutarSqlSeguro("update emergencias set operador_id = " + guardado.getId() + " where operador_id = " + usuario.getId());
        ejecutarSqlSeguro("update historial_emergencias set operador_id = " + guardado.getId() + " where operador_id = " + usuario.getId());
        ejecutarSqlSeguro("update entidades_notificadas set operador_id = " + guardado.getId() + " where operador_id = " + usuario.getId());
    }

    private void limpiarFilasLegacyVacias() {
        ejecutarSqlSeguro("delete from operadores where correo is null");
        ejecutarSqlSeguro("delete from superadmins where correo is null");
    }

    private void limpiarCuentasNoCiudadanasDeUsuarios() {
        ejecutarSqlSeguro("delete from solicitudes_operador where usuario_id in (select id from usuarios where rol in ('OPERADOR', 'OPERADOR_PENDIENTE', 'ADMIN', 'SUPERADMIN'))");
        ejecutarSqlSeguro("delete from usuarios where rol in ('OPERADOR', 'OPERADOR_PENDIENTE', 'ADMIN', 'SUPERADMIN')");
    }

    private void ejecutarSqlSeguro(String sql) {
        try {
            jdbcTemplate.execute(sql);
        } catch (Exception ignored) {
        }
    }
}
