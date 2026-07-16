package botondepanico.controller;

import botondepanico.model.Evidencia;
import botondepanico.model.Operador;
import botondepanico.model.SuperAdmin;
import botondepanico.service.EvidenciaService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EvidenciaArchivoController {

    private final EvidenciaService evidenciaService;

    public EvidenciaArchivoController(EvidenciaService evidenciaService) {
        this.evidenciaService = evidenciaService;
    }

    @GetMapping("/evidencias/{id}/archivo")
    public ResponseEntity<byte[]> archivo(@PathVariable Long id, HttpSession session) {
        Operador operador = (Operador) session.getAttribute("operador");
        SuperAdmin admin = (SuperAdmin) session.getAttribute("admin");
        if (operador == null && admin == null) {
            return ResponseEntity.status(401).build();
        }

        Evidencia evidencia = evidenciaService.obtener(id).orElse(null);
        if (evidencia == null || evidencia.getContenido() == null) {
            return ResponseEntity.notFound().build();
        }

        MediaType tipo = evidencia.getContentType() != null
            ? MediaType.parseMediaType(evidencia.getContentType())
            : MediaType.APPLICATION_OCTET_STREAM;

        return ResponseEntity.ok()
            .contentType(tipo)
            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + evidencia.getNombreArchivo() + "\"")
            .body(evidencia.getContenido());
    }
}