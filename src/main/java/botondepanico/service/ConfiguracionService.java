package botondepanico.service;

import botondepanico.model.ConfiguracionSistema;
import botondepanico.repository.ConfiguracionSistemaRepository;
import org.springframework.stereotype.Service;

@Service
public class ConfiguracionService {

    private final ConfiguracionSistemaRepository repository;

    public ConfiguracionService(ConfiguracionSistemaRepository repository) {
        this.repository = repository;
    }

    public ConfiguracionSistema obtener() {
        return repository.findAll().stream().findFirst().orElseGet(this::crearDefecto);
    }

    public ConfiguracionSistema guardar(ConfiguracionSistema config) {
        ConfiguracionSistema actual = obtener();
        config.setId(actual.getId());
        return repository.save(config);
    }

    private ConfiguracionSistema crearDefecto() {
        ConfiguracionSistema config = new ConfiguracionSistema();
        config.setNombreSistema("Boton de Panico");
        config.setCorreoSoporte("soporte@botonpanico.com");
        config.setTelefonoContacto("105");
        config.setDireccion("Lima");
        config.setCorreoComisaria("comisaria.alertas@gmail.com");
        config.setCorreoSamu("samu.alertas@gmail.com");
        config.setCorreoBomberos("bomberos.alertas@gmail.com");
        config.setCorreoAutomatico(true);
        config.setNotificarUsuario(true);
        config.setNotificarOperador(true);
        config.setNotificarAdmin(true);
        config.setTiempoMaximoAtencion(30);
        config.setRadioBusquedaKm(5);
        config.setDuracionSesion(60);
        config.setRegistroActividad(true);
        return repository.save(config);
    }
}
