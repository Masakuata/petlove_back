package xatal.sharedz.services;

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

    public Miembro addMiembro(Miembro miembro) {
        return this.miembros.save(miembro);
    }

    public Miembro login(Miembro miembro) {
        Optional<Miembro> miembroOptional = this.miembros.login(miembro.getEmail(), miembro.getPassword());
        if (miembroOptional.isPresent()) {
            return miembro;
        }
        return null;
    }

    public boolean deleteMiembro(String email) {
        Optional<Miembro> miembroOptional = this.miembros.findOneByEmail(email);
        if (miembroOptional.isPresent()) {
            this.miembros.deleteMiembroByEmail(email);
            return true;
        }
        return false;
    }
}
