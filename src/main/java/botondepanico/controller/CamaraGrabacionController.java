package botondepanico.controller;

import botondepanico.model.Operador;
import botondepanico.model.SuperAdmin;
import jakarta.servlet.http.HttpSession;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping({"/admin/camaras", "/operador/camaras"})
public class CamaraGrabacionController {

    private final Map<Long, Process> procesosActivos = new ConcurrentHashMap<>();
    private final Map<Long, Path> archivosActivos = new ConcurrentHashMap<>();

    @PostMapping("/{id}/grabar/iniciar")
    public ResponseEntity<?> iniciar(@PathVariable Long id,
                                      @RequestParam String urlStream,
                                      HttpSession session) throws IOException {
        if (!autenticado(session)) {
            return ResponseEntity.status(401).body("Sesión expirada");
        }

        if (procesosActivos.containsKey(id)) {
            return ResponseEntity.badRequest().body("Ya hay una grabación en curso para esta cámara");
        }

        Path archivo = Files.createTempFile("camara_" + id + "_", ".mp4");

        ProcessBuilder pb = new ProcessBuilder(
            "ffmpeg", "-y", "-i", urlStream, "-c:v", "copy", archivo.toAbsolutePath().toString()
        );
        pb.redirectErrorStream(true);
        Process proceso = pb.start();

        procesosActivos.put(id, proceso);
        archivosActivos.put(id, archivo);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/grabar/detener")
    public ResponseEntity<?> detener(@PathVariable Long id, HttpSession session) throws IOException, InterruptedException {
        if (!autenticado(session)) {
            return ResponseEntity.status(401).body("Sesión expirada");
        }

        Process proceso = procesosActivos.remove(id);
        Path archivo = archivosActivos.remove(id);

        if (proceso == null || archivo == null) {
            return ResponseEntity.badRequest().body("No hay ninguna grabación activa para esta cámara");
        }

        // Pide a ffmpeg que cierre el archivo correctamente antes de forzar el cierre
        proceso.getOutputStream().write("q".getBytes());
        proceso.getOutputStream().flush();
        proceso.waitFor(5, TimeUnit.SECONDS);
        if (proceso.isAlive()) {
            proceso.destroy();
        }

        Resource recurso = new UrlResource(archivo.toUri());

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"camara_" + id + ".mp4\"")
            .contentType(MediaType.parseMediaType("video/mp4"))
            .body(recurso);
    }

    private boolean autenticado(HttpSession session) {
        Operador operador = (Operador) session.getAttribute("operador");
        SuperAdmin admin = (SuperAdmin) session.getAttribute("admin");
        return operador != null || admin != null;
    }
}