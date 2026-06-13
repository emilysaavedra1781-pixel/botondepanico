package botondepanico.service;

import botondepanico.model.EstadoOperador;
import botondepanico.model.Operador;
import botondepanico.model.SuperAdmin;
import botondepanico.model.Usuario;
import botondepanico.repository.OperadorRepository;
import botondepanico.repository.SuperAdminRepository;
import botondepanico.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private OperadorRepository operadorRepository;

    @Autowired
    private SuperAdminRepository superAdminRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public boolean registrar(Usuario usuario) {
        if (usuarioRepository.existsByCelular(usuario.getCelular()) ||
            usuarioRepository.existsByDni(usuario.getDni()) ||
            usuarioRepository.existsByCorreo(usuario.getCorreo())) {
            return false;
        }
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        usuarioRepository.save(usuario);
        return true;
    }

    public Usuario registrarUsuario(Usuario usuario) {
        prepararCuenta(usuario, "USUARIO", "USUARIO", "ACTIVO");
        return registrarYRetornar(usuario);
    }

    public Operador registrarOperadorPendiente(Operador operador) {
        if (existeIdentidadRegistrada(operador.getCelular(), operador.getDni(), operador.getCorreo())) {
            return null;
        }
        operador.setEstado(EstadoOperador.PENDIENTE);
        operador.setEnLinea(false);
        operador.setPassword(passwordEncoder.encode(operador.getPassword()));
        Operador guardado = operadorRepository.save(operador);
        return guardado;
    }

    public Optional<Usuario> buscarPorCelular(String celular) {
        return usuarioRepository.findByCelular(celular);
    }

    public Optional<Usuario> buscarPorCorreo(String correo) {
        return usuarioRepository.findByCorreo(correo);
    }

    public List<Operador> listarOperadores() {
        return operadorRepository.findAllByOrderByFechaRegistroDesc();
    }

    public List<Usuario> listarUsuarios() {
        return usuarioRepository.findByRolOrderByFechaRegistroDesc("USUARIO");
    }

    public void aprobarOperador(Long id) {
        operadorRepository.findById(id).ifPresent(operador -> {
            operador.setEstado(EstadoOperador.APROBADO);
            operador.setEnLinea(true);
            operador.setFechaAprobacion(LocalDateTime.now());
            operadorRepository.save(operador);
        });
    }

    public void rechazarOperador(Long id) {
        operadorRepository.findById(id).ifPresent(operador -> {
            operador.setEstado(EstadoOperador.RECHAZADO);
            operador.setEnLinea(false);
            operadorRepository.save(operador);
        });
    }

    public void bloquearUsuario(Long id) {
        usuarioRepository.findById(id).ifPresent(usuario -> {
            usuario.setEstadoCuenta("BLOQUEADO");
            usuarioRepository.save(usuario);
        });
    }

    public void activarUsuario(Long id) {
        usuarioRepository.findById(id).ifPresent(usuario -> {
            usuario.setEstadoCuenta("ACTIVO");
            usuarioRepository.save(usuario);
        });
    }

    public Usuario login(String celular, String contrasena) {
        Optional<Usuario> optional = usuarioRepository.findByCelular(celular);
        return autenticar(optional, contrasena);
    }

    public Usuario loginPorCorreo(String correo, String contrasena) {
        Optional<Usuario> optional = usuarioRepository.findByCorreo(correo);
        return autenticar(optional, contrasena);
    }

    public Operador loginOperadorPorCorreo(String correo, String contrasena) {
        Optional<Operador> optional = operadorRepository.findByCorreo(correo);
        if (optional.isPresent()) {
            Operador operador = optional.get();
            if (passwordEncoder.matches(contrasena, operador.getPassword())) {
                operador.setUltimoAcceso(LocalDateTime.now());
                operadorRepository.save(operador);
                return operador;
            }
        }
        return null;
    }

    public SuperAdmin loginSuperAdminPorCorreo(String correo, String contrasena) {
        Optional<SuperAdmin> optional = superAdminRepository.findByCorreo(correo);
        if (optional.isPresent()) {
            SuperAdmin admin = optional.get();
            if (passwordEncoder.matches(contrasena, admin.getPassword())) {
                admin.setUltimoAcceso(LocalDateTime.now());
                superAdminRepository.save(admin);
                return admin;
            }
        }
        return null;
    }

    public boolean existeIdentidadRegistrada(Usuario usuario) {
        return existeIdentidadRegistrada(usuario.getCelular(), usuario.getDni(), usuario.getCorreo());
    }

    private boolean existeIdentidadRegistrada(String celular, String dni, String correo) {
        return usuarioRepository.existsByCelular(celular) ||
               usuarioRepository.existsByDni(dni) ||
               usuarioRepository.existsByCorreo(correo) ||
               operadorRepository.existsByCelular(celular) ||
               operadorRepository.existsByDni(dni) ||
               operadorRepository.existsByCorreo(correo) ||
               superAdminRepository.existsByCorreo(correo);
    }

    private Usuario registrarYRetornar(Usuario usuario) {
        if (existeIdentidadRegistrada(usuario)) {
            return null;
        }
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        return usuarioRepository.save(usuario);
    }

    private void prepararCuenta(Usuario usuario, String tipoCuenta, String rol, String estadoCuenta) {
        usuario.setTipoCuenta(tipoCuenta);
        usuario.setRol(rol);
        usuario.setEstadoCuenta(estadoCuenta);
    }

    private Usuario autenticar(Optional<Usuario> optional, String contrasena) {
        if (optional.isPresent()) {
            Usuario usuario = optional.get();
            if (passwordEncoder.matches(contrasena, usuario.getPassword())) {
                usuario.setUltimoAcceso(LocalDateTime.now());
                usuarioRepository.save(usuario);
                return usuario;
            }
        }
        return null;
    }
}
