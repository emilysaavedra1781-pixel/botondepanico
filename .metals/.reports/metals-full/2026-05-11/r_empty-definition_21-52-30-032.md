error id: file:///C:/botondepanico/service/UsuarioService.java:_empty_/UsuarioRepository#existsByDni#
file:///C:/botondepanico/service/UsuarioService.java
empty definition using pc, found symbol in pc: _empty_/UsuarioRepository#existsByDni#
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 750
uri: file:///C:/botondepanico/service/UsuarioService.java
text:
```scala
package com.botondepanico.botondepanico.service;

import com.botondepanico.botondepanico.model.Usuario;
import com.botondepanico.botondepanico.repository.UsuarioRepository;
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

    // Registrar nuevo usuario
    public boolean registrar(Usuario usuario) {
        if (usuarioRepository.existsByCelular(usuario.getCelular()) ||
            usuarioRepository.exi@@stsByDni(usuario.getDni()) ||
            usuarioRepository.existsByCorreo(usuario.getCorreo())) {
            return false; // ya existe
        }
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        usuarioRepository.save(usuario);
        return true;
    }

    // Buscar usuario por celular (para login)
    public Optional<Usuario> buscarPorCelular(String celular) {
        return usuarioRepository.findByCelular(celular);
    }
}
```


#### Short summary: 

empty definition using pc, found symbol in pc: _empty_/UsuarioRepository#existsByDni#