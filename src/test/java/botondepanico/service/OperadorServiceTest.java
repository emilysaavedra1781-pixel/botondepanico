package botondepanico.service;

import botondepanico.model.*;
import botondepanico.repository.EmergenciaRepository;
import botondepanico.repository.HistorialEmergenciaRepository;
import botondepanico.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OperadorServiceTest {

    @Mock
    private EmergenciaRepository emergenciaRepository;

    @Mock
    private HistorialEmergenciaRepository historialRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private OperadorService operadorService;

    @Test
    void crearReporteTelefonicoMarcaEmergenciaComoTelefonica() {
        Usuario usuario = new Usuario();
        usuario.setId(7L);
        usuario.setNombre("Ana");
        usuario.setApellido("Gomez");
        usuario.setCelular("999999999");

        when(usuarioRepository.findByCelular("999999999")).thenReturn(Optional.of(usuario));
        when(emergenciaRepository.save(any(Emergencia.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(historialRepository.save(any(HistorialEmergencia.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Emergencia emergencia = operadorService.crearReporteTelefonico(
            new Operador(),
            "Ana",
            "Gomez",
            "999999999",
            "12345678",
            "San Isidro",
            "Av. Central",
            "ROBO_ASALTO",
            "ALTA",
            "Llamada recibida por telefono"
        );

        assertEquals(OrigenEmergencia.TELEFONICO, emergencia.getOrigen());
        assertEquals(EstadoEmergencia.PENDIENTE, emergencia.getEstado());
        assertEquals("ROBO_ASALTO", emergencia.getTipoEmergencia());
        assertEquals(PrioridadEmergencia.ALTA, emergencia.getPrioridad());
    }
}
