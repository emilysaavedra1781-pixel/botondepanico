package botondepanico.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.ToString;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "emergencias")
public class Emergencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    @ToString.Exclude
    private Usuario usuario;

    @Column(name = "tipo_emergencia", nullable = false)
    private String tipoEmergencia;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoEmergencia estado;

    private String latitud;
    private String longitud;
    private String ubicacion;
    private String distrito;
    private String direccion;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "descripcion_operador", columnDefinition = "TEXT")
    private String descripcionOperador;

    @Enumerated(EnumType.STRING)
    @Column
    private PrioridadEmergencia prioridad;

    @Column(name = "entidad_notificada")
    private String entidadNotificada;

    @Column(name = "correo_entidad_notificada")
    private String correoEntidadNotificada;

    @Column(name = "motivo_rechazo", columnDefinition = "TEXT")
    private String motivoRechazo;

    @Column(name = "foto_video")
    private String fotoVideo;

    @ManyToOne
    @JoinColumn(name = "operador_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    @ToString.Exclude
    private Operador operadorAsignado;

    @OneToOne(mappedBy = "emergencia", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private Ubicacion ubicacionGps;

    @OneToMany(mappedBy = "emergencia", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("fechaEnvio DESC")
    @ToString.Exclude
    private List<Evidencia> evidencias = new ArrayList<>();

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @PrePersist
    public void prePersist() {
        if (this.fecha == null) {
            this.fecha = LocalDateTime.now();
        }
        if (this.estado == null) {
            this.estado = EstadoEmergencia.PENDIENTE;
        }
        if (this.prioridad == null) {
            this.prioridad = PrioridadEmergencia.MEDIA;
        }
        if (this.fechaActualizacion == null) {
            this.fechaActualizacion = LocalDateTime.now();
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.fechaActualizacion = LocalDateTime.now();
    }
}
