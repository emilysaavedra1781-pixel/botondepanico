package botondepanico.dto;

import botondepanico.model.Operador;
import botondepanico.model.SuperAdmin;
import botondepanico.model.Usuario;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class AuthResponseDTO implements Serializable {
    private Long id;
    private String nombre;
    private String apellido;
    private String correo;
    private String celular;
    private String dni;
    private String rol;
    private String distrito;
    private String tipoCuenta;
    private String estadoCuenta;
    private LocalDateTime fechaRegistro;

    public String getNombreCompleto() {
        return (nombre != null ? nombre : "") + " " + (apellido != null ? apellido : "");
    }

    public static AuthResponseDTO fromUsuario(Usuario usuario) {
        return AuthResponseDTO.builder()
                .id(usuario.getId())
                .nombre(usuario.getNombre())
                .apellido(usuario.getApellido())
                .correo(usuario.getCorreo())
                .celular(usuario.getCelular())
                .dni(usuario.getDni())
                .rol("USUARIO")
                .distrito(usuario.getDistrito())
                .tipoCuenta(usuario.getTipoCuenta())
                .estadoCuenta(usuario.getEstadoCuenta())
                .fechaRegistro(usuario.getFechaRegistro())
                .build();
    }

    public static AuthResponseDTO fromOperador(Operador operador) {
        return AuthResponseDTO.builder()
                .id(operador.getId())
                .nombre(operador.getNombre())
                .apellido(operador.getApellido())
                .correo(operador.getCorreo())
                .celular(operador.getCelular())
                .dni(operador.getDni())
                .rol("OPERADOR")
                .distrito(operador.getDistrito())
                .tipoCuenta("OPERADOR")
                .estadoCuenta(operador.getEstadoCuenta())
                .fechaRegistro(operador.getFechaRegistro())
                .build();
    }

    public static AuthResponseDTO fromAdmin(SuperAdmin admin) {
        return AuthResponseDTO.builder()
                .id(admin.getId())
                .nombre(admin.getNombre())
                .apellido(admin.getApellido())
                .correo(admin.getCorreo())
                .celular(admin.getCelular())
                .dni(admin.getDni())
                .rol("ADMIN")
                .distrito("")
                .tipoCuenta("ADMIN")
                .estadoCuenta(admin.getEstadoCuenta())
                .fechaRegistro(admin.getFechaRegistro())
                .build();
    }
}
