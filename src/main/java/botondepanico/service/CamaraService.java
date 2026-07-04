package botondepanico.service;

import botondepanico.model.Camara;
import botondepanico.repository.CamaraRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CamaraService {

    private final CamaraRepository camaraRepository;

    public CamaraService(CamaraRepository camaraRepository) {
        this.camaraRepository = camaraRepository;
    }

    public List<Camara> listarActivas() {
        return camaraRepository.findByActivaTrueOrderByNombreAsc();
    }

    public List<Camara> listarTodas() {
        return camaraRepository.findAllByOrderByNombreAsc();
    }

    public Optional<Camara> obtener(Long id) {
        return camaraRepository.findById(id);
    }

    public Camara guardar(Camara camara) {
        return camaraRepository.save(camara);
    }

    public void eliminar(Long id) {
        camaraRepository.deleteById(id);
    }

    public void desactivar(Long id) {
        camaraRepository.findById(id).ifPresent(camara -> {
            camara.setActiva(false);
            camaraRepository.save(camara);
        });
    }
}
