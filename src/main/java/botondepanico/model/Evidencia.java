package botondepanico.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "evidencias")
public class Evidencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "emergencia_id", nullable = false)
    @ToString.Exclude
    private Emergencia emergencia;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    @ToString.Exclude
    private Usuario usuario;

    @Column(nullable = false)
    private String tipo;

    @Column(name = "nombre_archivo", nullable = false)
    private String nombreArchivo;

    @Column(name = "ruta_archivo", nullable = false)
    private String rutaArchivo;

    @Column(name = "content_type")
    private String contentType;

    @Column(name = "tamano_bytes")
    private Long tamanoBytes;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "fecha_envio", nullable = false)
    private LocalDateTime fechaEnvio;

    // NUEVO: guarda el archivo directamente en la base de datos (sin depender de carpetas ni servicios externos)
    @Lob
    @Column(name = "contenido")
    @ToString.Exclude
    private byte[] contenido;

    @PrePersist
    public void prePersist() {
        if (this.fechaEnvio == null) {
            this.fechaEnvio = LocalDateTime.now();
        }
    }
}