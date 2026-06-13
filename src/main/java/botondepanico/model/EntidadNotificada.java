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
@Table(name = "entidades_notificadas")
public class EntidadNotificada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "emergencia_id", nullable = false)
    @ToString.Exclude
    private Emergencia emergencia;

    @ManyToOne
    @JoinColumn(name = "operador_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    @ToString.Exclude
    private Operador operador;

    @Column(nullable = false)
    private String entidad;

    @Column(nullable = false)
    private String correo;

    @Column(name = "fecha_notificacion", nullable = false)
    private LocalDateTime fechaNotificacion;

    @PrePersist
    public void prePersist() {
        if (fechaNotificacion == null) {
            fechaNotificacion = LocalDateTime.now();
        }
    }
}
