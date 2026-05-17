error id: file:///C:/Users/Emi/Pictures/Documents/botondepanico/src/main/java/botondepanico/service/EmergenciaService.java
file:///C:/Users/Emi/Pictures/Documents/botondepanico/src/main/java/botondepanico/service/EmergenciaService.java
### com.thoughtworks.qdox.parser.ParseException: syntax error @[62,1]

error in qdox parser
file content:
```java
offset: 2075
uri: file:///C:/Users/Emi/Pictures/Documents/botondepanico/src/main/java/botondepanico/service/EmergenciaService.java
text:
```scala
package botondepanico.service;

import botondepanico.model.Emergencia;
import botondepanico.model.Usuario;
import botondepanico.repository.EmergenciaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EmergenciaService {

    @Autowired
    private EmergenciaRepository emergenciaRepository;

    @Autowired
    private EmailService emailService;

    public void registrarEmergencia(Usuario usuario, String tipoEmergencia,
                                     String latitud, String longitud,
                                     String ubicacion, String fotoVideo) {
        // 1. Guardar en BD
        Emergencia emergencia = new Emergencia();
        emergencia.setUsuario(usuario);
        emergencia.setTipoEmergencia(tipoEmergencia);
        emergencia.setLatitud(latitud);
        emergencia.setLongitud(longitud);
        emergencia.setUbicacion(ubicacion);
        emergencia.setFotoVideo(fotoVideo);
        emergenciaRepository.save(emergencia);

        // 2. Enviar correo a la autoridad
        String correoAutoridad = obtenerCorreo(tipoEmergencia);
        emailService.enviarAlerta(
            correoAutoridad,
            usuario.getNombre() + " " + usuario.getApellido(),
            usuario.getCelular(),
            tipoEmergencia,
            latitud,
            longitud
        );
    }

    private String obtenerCorreo(String tipo) {
        return switch (tipo) {
            case "MEDICA"    -> "samu106@gmail.com";
            case "INCENDIO"  -> "bomberos116@gmail.com";
            case "SEGURIDAD" -> "policia105@gmail.com";
            default          -> "policia105@gmail.com";
        };
    }

    try {
    emailService.enviarAlerta(
        correoAutoridad,
        usuario.getNombre() + " " + usuario.getApellido(),
        usuario.getCelular(),
        tipoEmergencia,
        latitud,
        longitud
    );
    System.out.println("✅ Correo enviado a: " + correoAutoridad);
}@@ catch (Exception e) {
    System.out.println("❌ Error enviando correo: " + e.getMessage());
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

QDox parse error in file:///C:/Users/Emi/Pictures/Documents/botondepanico/src/main/java/botondepanico/service/EmergenciaService.java