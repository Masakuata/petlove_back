package xatal.sharedz.services;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import xatal.sharedz.entities.Miembro;
import xatal.sharedz.repositories.MiembroRepository;
import xatal.sharedz.structures.PublicMiembro;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MiembroService {
    private final MiembroRepository miembros;

    public MiembroService(MiembroRepository miembros) {
        this.miembros = miembros;
    }

    public List<Miembro> getAll() {
        return this.miembros.getAll();
    }

    public List<PublicMiembro> getAllPublic() {
        return this.miembros.getAll()
                .stream()
                .map(PublicMiembro::new)
                .collect(Collectors.toList());
    }

    public Miembro saveMiembro(Miembro miembro) {
        return this.miembros.save(miembro);
    }

    public Miembro login(Miembro miembro) {
        return this.miembros.login(miembro.getEmail(), miembro.getPassword()).orElse(null);
    }

    public Miembro getMiembroFromEmail(String email) {
        return this.miembros.findOneByEmail(email).orElse(null);
    }

    public Miembro getMiembroFromUsername(String username) {
        return this.miembros.findMiembroByUsername(username).orElse(null);
    }

    @Transactional
    public boolean deleteMiembro(String email) {
        Optional<Miembro> miembroOptional = this.miembros.findOneByEmail(email);
        if (miembroOptional.isPresent()) {
            return this.miembros.deleteByEmail(email) == 1;
        }
        return false;
    }

    public boolean isEmailUsed(String email) {
        return this.miembros.countByEmail(email) > 0;
    }

    public boolean isUsernameUsed(String username) {
        return this.miembros.countByUsername(username) > 0;
    }
}
