package xatal.petlove.services;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import xatal.petlove.entities.Usuario;
import xatal.petlove.repositories.UsuarioRepository;
import xatal.petlove.structures.PublicUsuario;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UsuarioService {
    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository miembros) {
        this.usuarioRepository = miembros;
    }

    public List<Usuario> getAll() {
        return this.usuarioRepository.getAll();
    }

    public List<PublicUsuario> getAllPublic() {
        return this.usuarioRepository.getAll()
                .stream()
                .map(PublicUsuario::new)
                .collect(Collectors.toList());
    }

    public Usuario saveUsuario(Usuario miembro) {
        return this.usuarioRepository.save(miembro);
    }

    public Usuario login(Usuario miembro) {
        return this.usuarioRepository.findByEmailAndPassword(miembro.getEmail(), miembro.getPassword()).orElse(null);
    }

    public Optional<Usuario> getUsuarioFromEmail(String email) {
        return this.usuarioRepository.findOneByEmail(email);
    }

    public Usuario getUsuarioFromUsername(String username) {
        return this.usuarioRepository.findUsuarioByUsername(username).orElse(null);
    }

    public Usuario getById(long id) {
        return this.usuarioRepository.findById(id).orElse(null);
    }

    @Transactional
    public boolean deleteUsuario(String email) {
        Optional<Usuario> usuarioOptional = this.usuarioRepository.findOneByEmail(email);
        return usuarioOptional.isPresent() && this.usuarioRepository.deleteByEmail(email) == 1;
    }

    public boolean isEmailUsed(String email) {
        return this.usuarioRepository.countByEmail(email) > 0;
    }

    public boolean isUsernameUsed(String username) {
        return this.usuarioRepository.countByUsername(username) > 0;
    }

    public boolean isUserAvailable(Usuario newUsuario) {
        return this.usuarioRepository.countByEmailAndUsername(newUsuario.getEmail(), newUsuario.getUsername()) == 0;
    }
}
