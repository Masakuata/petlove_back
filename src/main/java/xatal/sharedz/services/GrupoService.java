package xatal.sharedz.services;

import jakarta.persistence.PreRemove;
import org.springframework.stereotype.Service;
import xatal.sharedz.entities.Grupo;
import xatal.sharedz.entities.Miembro;
import xatal.sharedz.repositories.GrupoRepository;
import xatal.sharedz.repositories.MiembroRepository;

import java.util.Optional;

@Service
public class GrupoService {
    private final GrupoRepository grupos;
    private final MiembroService miembros;

    public GrupoService(GrupoRepository grupos, MiembroService miembros) {
        this.grupos = grupos;
        this.miembros = miembros;
    }

    public Grupo newGrupo(String grupoName, Miembro creador) {
        Grupo grupo = new Grupo();
        grupo.setName(grupoName);
        grupo.getMembers().add(creador);
        creador.getGrupos().add(grupo);
        grupo = this.grupos.save(grupo);
        this.miembros.saveMiembro(creador);
        return grupo;
    }

    public Grupo saveGrupo(Grupo grupo) {
        return this.grupos.save(grupo);
    }

    public boolean addMiembro(String grupoName, Miembro miembro) {
        Optional<Grupo> grupoOptional = this.grupos.getGrupoByName(grupoName);
        if (grupoOptional.isPresent()) {
            Grupo grupo = grupoOptional.get();
            grupo.getMembers().add(miembro);
            miembro.getGrupos().add(this.grupos.save(grupo));
            this.miembros.saveMiembro(miembro);
        }
        return grupoOptional.isPresent();
    }

    public boolean removeMiembro(String grupoName, Miembro miembro) {
        boolean removed = false;
        Optional<Grupo> grupoOptional = this.grupos.getGrupoByName(grupoName);
        if (grupoOptional.isPresent()) {
            Grupo grupo = grupoOptional.get();
            if (grupo.getMembers().contains(miembro)) {
                grupo.getMembers().remove(miembro);
                miembro.getGrupos().remove(grupo);
                this.miembros.saveMiembro(miembro);
                removed = true;
            }
            if (grupo.getMembers().isEmpty()) {
                this.deleteGrupo(grupo);
            }
        }
        return removed;
    }

    public boolean isMemberIn(String grupoName, Miembro miembro) {
        Optional<Grupo> grupoOptional = this.grupos.getGrupoByName(grupoName);
        return grupoOptional.isPresent() && grupoOptional.get().getMembers().contains(miembro);
    }

    public void deleteGrupo(Grupo grupo) {
        for (Miembro member : grupo.getMembers()) {
            member.getGrupos().remove(grupo);
            grupo.getMembers().remove(member);
            this.miembros.saveMiembro(member);
        }
        this.grupos.delete(grupo);
    }

    public boolean isNameUsed(String name) {
        return this.grupos.countByName(name) > 0;
    }
}
