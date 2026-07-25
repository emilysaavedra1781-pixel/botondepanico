package botondepanico.dto;

import botondepanico.model.EstadoEmergencia;
import botondepanico.model.OrigenEmergencia;
import botondepanico.model.PrioridadEmergencia;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class EmergenciaResumenDTO {
    private Long id;
    private String tipoEmergencia;
    private EstadoEmergencia estado;
    private PrioridadEmergencia prioridad;
    private String distrito;
    private String direccion;
    private String latitud;
    private String longitud;
    private LocalDateTime fecha;
    private Long usuarioId;
    private String usuarioNombre;
    private String usuarioApellido;
    private OrigenEmergencia origen;
    private String operadorNombre;
    private String entidadNotificada;

    public String getUsuarioNombreCompleto() {
        return (usuarioNombre != null ? usuarioNombre : "") + " " + (usuarioApellido != null ? usuarioApellido : "");
    }
}
