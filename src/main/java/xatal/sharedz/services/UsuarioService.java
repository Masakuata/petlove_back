package xatal.sharedz.services;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import xatal.sharedz.entities.Usuario;
import xatal.sharedz.repositories.UsuarioRepository;
import xatal.sharedz.structures.PublicUsuario;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UsuarioService {
    private final UsuarioRepository usuarios;

    public UsuarioService(UsuarioRepository miembros) {
        this.usuarios = miembros;
    }

    public List<Usuario> getAll() {
        return this.usuarios.getAll();
    }

    public List<PublicUsuario> getAllPublic() {
        return this.usuarios.getAll()
                .stream()
                .map(PublicUsuario::new)
                .collect(Collectors.toList());
    }

    public Usuario saveUsuario(Usuario miembro) {
        return this.usuarios.save(miembro);
    }

    public Usuario login(Usuario miembro) {
        return this.usuarios.findByEmailAndPassword(miembro.getEmail(), miembro.getPassword()).orElse(null);
    }

    public Usuario getUsuarioFromEmail(String email) {
        return this.usuarios.findOneByEmail(email).orElse(null);
    }

    public Usuario getUsuarioFromUsername(String username) {
        return this.usuarios.findUsuarioByUsername(username).orElse(null);
    }

    @Transactional
    public boolean deleteUsuario(String email) {
        Optional<Usuario> usuarioOptional = this.usuarios.findOneByEmail(email);
        return usuarioOptional.isPresent() && this.usuarios.deleteByEmail(email) == 1;
    }

    public boolean isEmailUsed(String email) {
        return this.usuarios.countByEmail(email) > 0;
    }

    public boolean isUsernameUsed(String username) {
        return this.usuarios.countByUsername(username) > 0;
    }

    public boolean isUserAvailable(Usuario newUsuario) {
        return this.usuarios.countByEmailAndUsername(newUsuario.getEmail(), newUsuario.getUsername()) < 0;
    }
}
