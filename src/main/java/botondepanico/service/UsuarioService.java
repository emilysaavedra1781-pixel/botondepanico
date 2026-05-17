package botondepanico.service;

import botondepanico.model.Usuario;
import botondepanico.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public boolean registrar(Usuario usuario) {
        if (usuarioRepository.existsByCelular(usuario.getCelular()) ||
            usuarioRepository.existsByDni(usuario.getDni()) ||
            usuarioRepository.existsByCorreo(usuario.getCorreo())) {
            return false;
        }
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        usuarioRepository.save(usuario);
        return true;
    }

    public Optional<Usuario> buscarPorCelular(String celular) {
        return usuarioRepository.findByCelular(celular);
    }

    // ✅ NUEVO — usado por AuthController para el login
    public Usuario login(String celular, String contrasena) {
        Optional<Usuario> optional = usuarioRepository.findByCelular(celular);

        if (optional.isPresent()) {
            Usuario usuario = optional.get();
            if (passwordEncoder.matches(contrasena, usuario.getPassword())) {
                return usuario;
            }
        }
        return null;
    }
}