package botondepanico.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "configuracion_sistema")
public class ConfiguracionSistema {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre_sistema")
    private String nombreSistema;

    @Column(name = "correo_soporte")
    private String correoSoporte;

    @Column(name = "telefono_contacto")
    private String telefonoContacto;

    private String direccion;

    @Column(name = "correo_comisaria")
    private String correoComisaria;

    @Column(name = "correo_samu")
    private String correoSamu;

    @Column(name = "correo_bomberos")
    private String correoBomberos;

    @Column(name = "correo_automatico")
    private Boolean correoAutomatico;

    @Column(name = "notificar_usuario")
    private Boolean notificarUsuario;

    @Column(name = "notificar_operador")
    private Boolean notificarOperador;

    @Column(name = "notificar_admin")
    private Boolean notificarAdmin;

    @Column(name = "tiempo_maximo_atencion")
    private Integer tiempoMaximoAtencion;

    @Column(name = "radio_busqueda_km")
    private Integer radioBusquedaKm;

    @Column(name = "duracion_sesion")
    private Integer duracionSesion;

    @Column(name = "registro_actividad")
    private Boolean registroActividad;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @PrePersist
    @PreUpdate
    public void touch() {
        fechaActualizacion = LocalDateTime.now();
    }
}
