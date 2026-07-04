error id: file:///C:/Users/Emi/Pictures/Documents/botondepanico/src/main/java/botondepanico/service/UsuarioService.java
file:///C:/Users/Emi/Pictures/Documents/botondepanico/src/main/java/botondepanico/service/UsuarioService.java
### com.thoughtworks.qdox.parser.ParseException: syntax error @[72,2]

error in qdox parser
file content:
```java
offset: 2304
uri: file:///C:/Users/Emi/Pictures/Documents/botondepanico/src/main/java/botondepanico/service/UsuarioService.java
text:
```scala
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

        // Todos los registrados serán usuarios normales
        usuario.setRol("USUARIO");

        usuario.setPassword(
            passwordEncoder.encode(usuario.getPassword())
        );

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

<<@@<<<<< HEAD
    public Usuario login(String celular, String contrasena) {

        Optional<Usuario> optional =
            usuarioRepository.findByCelular(celular);
=======
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
>>>>>>> 5ad7d50f5c5809f37a0fbf4b5d12f5963ef2b300

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
<<<<<<< HEAD

            if (passwordEncoder.matches(
                    contrasena,
                    usuario.getPassword())) {

=======
            if (passwordEncoder.matches(contrasena, usuario.getPassword())) {
                usuario.setUltimoAcceso(LocalDateTime.now());
                usuarioRepository.save(usuario);
>>>>>>> 5ad7d50f5c5809f37a0fbf4b5d12f5963ef2b300
                return usuario;
            }
        }

        return null;
    }
}

```

```



#### Error stacktrace:

```
com.thoughtworks.qdox.parser.impl.Parser.yyerror(Parser.java:2025)
	com.thoughtworks.qdox.parser.impl.Parser.yyparse(Parser.java:2147)
	com.thoughtworks.qdox.parser.impl.Parser.parse(Parser.java:2006)
	com.thoughtworks.qdox.library.SourceLibrary.parse(SourceLibrary.java:232)
	com.thoughtworks.qdox.library.SourceLibrary.parse(SourceLibrary.java:190)
	com.thoughtworks.qdox.library.SourceLibrary.addSource(SourceLibrary.java:94)
	com.thoughtworks.qdox.library.SourceLibrary.addSource(SourceLibrary.java:89)
	com.thoughtworks.qdox.library.SortedClassLibraryBuilder.addSource(SortedClassLibraryBuilder.java:162)
	com.thoughtworks.qdox.JavaProjectBuilder.addSource(JavaProjectBuilder.java:174)
	scala.meta.internal.mtags.JavaMtags.indexRoot(JavaMtags.scala:49)
	scala.meta.internal.metals.SemanticdbDefinition$.foreachWithReturnMtags(SemanticdbDefinition.scala:99)
	scala.meta.internal.metals.Indexer.indexSourceFile(Indexer.scala:560)
	scala.meta.internal.metals.Indexer.$anonfun$reindexWorkspaceSources$3(Indexer.scala:691)
	scala.meta.internal.metals.Indexer.$anonfun$reindexWorkspaceSources$3$adapted(Indexer.scala:688)
	scala.collection.IterableOnceOps.foreach(IterableOnce.scala:630)
	scala.collection.IterableOnceOps.foreach$(IterableOnce.scala:628)
	scala.collection.AbstractIterator.foreach(Iterator.scala:1313)
	scala.meta.internal.metals.Indexer.reindexWorkspaceSources(Indexer.scala:688)
	scala.meta.internal.metals.MetalsLspService.$anonfun$onChange$2(MetalsLspService.scala:940)
	scala.runtime.java8.JFunction0$mcV$sp.apply(JFunction0$mcV$sp.scala:18)
	scala.concurrent.Future$.$anonfun$apply$1(Future.scala:691)
	scala.concurrent.impl.Promise$Transformation.run(Promise.scala:500)
	java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1136)
	java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:635)
	java.base/java.lang.Thread.run(Thread.java:842)
```
#### Short summary: 

QDox parse error in file:///C:/Users/Emi/Pictures/Documents/botondepanico/src/main/java/botondepanico/service/UsuarioService.java