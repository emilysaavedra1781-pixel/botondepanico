error id: file:///C:/Users/Emi/Pictures/Documents/botondepanico/src/main/java/botondepanico/controller/AuthController.java
file:///C:/Users/Emi/Pictures/Documents/botondepanico/src/main/java/botondepanico/controller/AuthController.java
### com.thoughtworks.qdox.parser.ParseException: syntax error @[103,2]

error in qdox parser
file content:
```java
offset: 3765
uri: file:///C:/Users/Emi/Pictures/Documents/botondepanico/src/main/java/botondepanico/controller/AuthController.java
text:
```scala
package botondepanico.controller;

import botondepanico.model.EstadoOperador;
import botondepanico.model.EstadoUsuario;
import botondepanico.model.Operador;
import botondepanico.model.SuperAdmin;
import botondepanico.model.Usuario;
import botondepanico.service.UsuarioService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @PostMapping("/login")
    public String procesarLogin(@RequestParam String correo,
                                @RequestParam String contrasena,
                                HttpSession session,
                                Model model) {
<<<<<<< HEAD

        Usuario usuario = usuarioService.login(celular, contrasena);

        if (usuario != null) {

            session.setAttribute("usuario", usuario);

            String rol = usuario.getRol();

            // 🧑‍💼 ADMIN / OPERADOR → panel principal
            if ("ADMIN".equals(rol) || "OPERADOR".equals(rol)) {
                return "redirect:/home";
            }

            // 👤 USUARIO NORMAL → dashboard
            return "redirect:/";
        }

        model.addAttribute("error", "Celular o contraseña incorrectos");
=======
        Usuario usuario = usuarioService.loginPorCorreo(correo, contrasena);
        if (usuario != null && "USUARIO".equalsIgnoreCase(usuario.getRol())) {
            if ("BLOQUEADO".equalsIgnoreCase(usuario.getEstadoCuenta())) {
                model.addAttribute("error", "Tu cuenta no se encuentra habilitada para acceder");
                return "login";
            }
            session.setAttribute("usuario", usuario);
            return "redirect:/usuario/dashboard";
        }

        Operador operador = usuarioService.loginOperadorPorCorreo(correo, contrasena);
        if (operador != null) {
            if (operador.getEstado() == EstadoOperador.PENDIENTE) {
                return "redirect:/operador-pendiente";
            }
            if (operador.getEstado() == EstadoOperador.RECHAZADO || operador.getEstado() == EstadoOperador.BLOQUEADO) {
                model.addAttribute("error", "Tu cuenta no se encuentra habilitada para acceder");
                return "login";
            }
            session.setAttribute("operador", operador);
            return "redirect:/operador/dashboard";
        }

        SuperAdmin admin = usuarioService.loginSuperAdminPorCorreo(correo, contrasena);
        if (admin != null) {
            if (admin.getEstado() == EstadoUsuario.BLOQUEADO || admin.getEstado() == EstadoUsuario.INACTIVO) {
                model.addAttribute("error", "Tu cuenta no se encuentra habilitada para acceder");
                return "login";
            }
            session.setAttribute("admin", admin);
            return "redirect:/admin/dashboard";
        }

        model.addAttribute("error", "Correo o contrasena incorrectos");
>>>>>>> 5ad7d50f5c5809f37a0fbf4b5d12f5963ef2b300
        return "login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    @GetMapping("/registro")
    public String seleccionRegistro() {
        return "registro";
    }

<<@@<<<<< HEAD
    @PostMapping("/registro")
    public String registrar(@ModelAttribute Usuario usuario,
                            Model model) {

        boolean exito = usuarioService.registrar(usuario);

        if (exito) {
            return "redirect:/login";
        }

        model.addAttribute("error",
                "El celular, DNI o correo ya está registrado");

        return "registro";
=======
    @GetMapping("/registro-usuario")
    public String registroUsuario() {
        return "registro-usuario";
>>>>>>> 5ad7d50f5c5809f37a0fbf4b5d12f5963ef2b300
    }

    @PostMapping("/registro-usuario")
    public String registrarUsuario(@RequestParam String nombreCompleto,
                                   @RequestParam String dni,
                                   @RequestParam String celular,
                                   @RequestParam String distrito,
                                   @RequestParam String correo,
                                   @RequestParam String password,
                                   @RequestParam String confirmarPassword,
                                   HttpSession session,
                                   Model model) {
        if (!password.equals(confirmarPassword)) {
            model.addAttribute("error", "Las contrasenas no coinciden");
            return "registro-usuario";
        }

        Usuario usuario = crearUsuarioBase(nombreCompleto, dni, celular, correo, password);
        usuario.setDistrito(distrito);

        Usuario guardado = usuarioService.registrarUsuario(usuario);
        if (guardado == null) {
            model.addAttribute("error", "El celular, DNI o correo ya esta registrado");
            return "registro-usuario";
        }

        session.setAttribute("usuario", guardado);
        return "redirect:/usuario/dashboard";
    }

    @GetMapping("/registro-operador")
    public String registroOperador() {
        return "registro-operador";
    }

    @PostMapping("/registro-operador")
    public String registrarOperador(@RequestParam String nombreCompleto,
                                    @RequestParam String dni,
                                    @RequestParam String celular,
                                    @RequestParam String correo,
                                    @RequestParam String password,
                                    @RequestParam String confirmarPassword,
                                    Model model) {
        if (!password.equals(confirmarPassword)) {
            model.addAttribute("error", "Las contrasenas no coinciden");
            return "registro-operador";
        }

        Operador operador = crearOperadorBase(nombreCompleto, dni, celular, correo, password);

        Operador guardado = usuarioService.registrarOperadorPendiente(operador);
        if (guardado == null) {
            model.addAttribute("error", "El celular, DNI o correo ya esta registrado");
            return "registro-operador";
        }

        return "redirect:/operador-pendiente";
    }

    @GetMapping("/operador-pendiente")
    public String operadorPendiente() {
        return "operador-pendiente";
    }

    private Usuario crearUsuarioBase(String nombreCompleto, String dni, String celular, String correo, String password) {
        String limpio = nombreCompleto == null ? "" : nombreCompleto.trim().replaceAll("\\s+", " ");
        String[] partes = limpio.split(" ", 2);

        Usuario usuario = new Usuario();
        usuario.setNombre(partes.length > 0 ? partes[0] : "");
        usuario.setApellido(partes.length > 1 ? partes[1] : "");
        usuario.setDni(dni);
        usuario.setCelular(celular);
        usuario.setCorreo(correo);
        usuario.setPassword(password);
        return usuario;
    }

    private Operador crearOperadorBase(String nombreCompleto, String dni, String celular, String correo, String password) {
        String limpio = nombreCompleto == null ? "" : nombreCompleto.trim().replaceAll("\\s+", " ");
        String[] partes = limpio.split(" ", 2);

        Operador operador = new Operador();
        operador.setNombre(partes.length > 0 ? partes[0] : "");
        operador.setApellido(partes.length > 1 ? partes[1] : "");
        operador.setDni(dni);
        operador.setCelular(celular);
        operador.setCorreo(correo);
        operador.setPassword(password);
        return operador;
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

QDox parse error in file:///C:/Users/Emi/Pictures/Documents/botondepanico/src/main/java/botondepanico/controller/AuthController.java