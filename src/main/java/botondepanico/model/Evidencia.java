package botondepanico.model;

import jakarta.persistence.*;
import jakarta.persistence.*;
import jakarta.persistence.Index;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.ToString;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
@Entity
@Table(name = "evidencias", indexes = {
    @Index(name = "idx_evidencias_emergencia_id", columnList = "emergencia_id")
})
public class Evidencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "emergencia_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Emergencia emergencia;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
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

    // Guarda el archivo directo en la base de datos como BYTEA (no como Large Object/OID)
    @JdbcTypeCode(SqlTypes.VARBINARY)
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "contenido", columnDefinition = "bytea")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private byte[] contenido;

    @PrePersist
    public void prePersist() {
        if (this.fechaEnvio == null) {
            this.fechaEnvio = LocalDateTime.now();
        }
    }
}