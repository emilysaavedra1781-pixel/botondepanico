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
@Table(name = "operadores")
public class Operador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String apellido;
    private String correo;
    private String celular;
    private String dni;
    private String distrito;

    @ToString.Exclude
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoOperador estado;

    @Column(name = "en_linea", nullable = false)
    private boolean enLinea;

    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro;

    @Column(name = "fecha_solicitud")
    private LocalDateTime fechaSolicitud;

    @Column(name = "fecha_aprobacion")
    private LocalDateTime fechaAprobacion;

    @Column(name = "ultimo_acceso")
    private LocalDateTime ultimoAcceso;

    @PrePersist
    public void prePersist() {
        if (this.estado == null) {
            this.estado = EstadoOperador.PENDIENTE;
        }
        if (this.fechaRegistro == null) {
            this.fechaRegistro = LocalDateTime.now();
        }
        if (this.fechaSolicitud == null) {
            this.fechaSolicitud = this.fechaRegistro;
        }
    }

    public String getRol() {
        return "OPERADOR";
    }

    public String getTipoCuenta() {
        return "OPERADOR";
    }

    public String getEstadoCuenta() {
        if (estado == EstadoOperador.APROBADO) return "ACTIVO";
        if (estado == EstadoOperador.RECHAZADO) return "RECHAZADO";
        if (estado == EstadoOperador.BLOQUEADO) return "BLOQUEADO";
        return "PENDIENTE";
    }
}
