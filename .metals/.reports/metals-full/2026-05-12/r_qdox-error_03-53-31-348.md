error id: file:///C:/botondepanico/src/main/java/botondepanico/config/SecurityConfig.java
file:///C:/botondepanico/src/main/java/botondepanico/config/SecurityConfig.java
### com.thoughtworks.qdox.parser.ParseException: syntax error @[1,1]

error in qdox parser
file content:
```java
offset: 1
uri: file:///C:/botondepanico/src/main/java/botondepanico/config/SecurityConfig.java
text:
```scala
<@@!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>Home - Botón de Pánico</title>
    <link rel="stylesheet" 
          href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css">
    <style>
        .sidebar {
            min-height: 100vh;
            background-color: #c0392b;
            width: 220px;
        }
        .sidebar a {
            color: white;
            text-decoration: none;
            padding: 12px 20px;
            display: block;
        }
        .sidebar a:hover {
            background-color: #a93226;
        }
        .btn-emergencia {
            height: 150px;
            font-size: 1.2rem;
            font-weight: bold;
            border-radius: 15px;
        }
    </style>
</head>
<body>
<div class="d-flex">

    <!-- Sidebar -->
    <div class="sidebar d-flex flex-column p-3">
        <h5 class="text-white text-center mb-4">🆘 Botón de Pánico</h5>
        <a href="/home">🏠 Inicio</a>
        <a href="/camara">📷 Cámara</a>
        <a href="/notificaciones">🔔 Notificaciones</a>
        <div class="mt-auto">
            <a th:href="@{/logout}">🚪 Cerrar Sesión</a>
        </div>
    </div>

    <!-- Contenido principal -->
    <div class="flex-grow-1 p-4">
        <h4 class="mb-1">Bienvenido,</h4>
        <h2 class="text-danger mb-4" 
            th:text="${nombre} + ' ' + ${apellido}">Nombre Apellido</h2>

        <h5 class="mb-3">¿Qué tipo de emergencia tienes?</h5>

        <div class="row g-3">
            <div class="col-md-6">
                <button class="btn btn-danger w-100 btn-emergencia">
                    🚑 SAMU<br><small>Emergencia Médica — 106</small>
                </button>
            </div>
            <div class="col-md-6">
                <button class="btn btn-warning w-100 btn-emergencia">
                    🔥 Bomberos<br><small>Incendio — 116</small>
                </button>
            </div>
            <div class="col-md-6">
                <button class="btn btn-dark w-100 btn-emergencia">
                    🚔 Policía Nacional<br><small>Seguridad / Delito — 105</small>
                </button>
            </div>
            <div class="col-md-6">
                <button class="btn btn-secondary w-100 btn-emergencia">
                    🏛️ Comisaría<br><small>Comisaría de tu distrito</small>
                </button>
            </div>
        </div>
    </div>

</div>
</body>
</html>
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

QDox parse error in file:///C:/botondepanico/src/main/java/botondepanico/config/SecurityConfig.java