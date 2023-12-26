package xatal.sharedz.services;

import org.springframework.stereotype.Service;
import xatal.sharedz.entities.Grupo;
import xatal.sharedz.entities.Usuario;
import xatal.sharedz.repositories.GrupoRepository;

import java.util.Optional;

@Service
public class GrupoService {
    private final GrupoRepository grupos;
    private final UsuarioService miembros;

    public GrupoService(GrupoRepository grupos, UsuarioService miembros) {
        this.grupos = grupos;
        this.miembros = miembros;
    }

    public Grupo newGrupo(String grupoName, Usuario creador) {
        Grupo grupo = new Grupo();
        grupo.setName(grupoName);
        grupo.getMembers().add(creador);
        creador.getGrupos().add(grupo);
        grupo = this.grupos.save(grupo);
        this.miembros.saveUsuario(creador);
        return grupo;
    }

    public Grupo saveGrupo(Grupo grupo) {
        return this.grupos.save(grupo);
    }

    public boolean addMiembro(String grupoName, Usuario miembro) {
        Optional<Grupo> grupoOptional = this.grupos.getGrupoByName(grupoName);
        if (grupoOptional.isPresent()) {
            Grupo grupo = grupoOptional.get();
            grupo.getMembers().add(miembro);
            miembro.getGrupos().add(this.grupos.save(grupo));
            this.miembros.saveUsuario(miembro);
        }
        return grupoOptional.isPresent();
    }

    public boolean removeMiembro(String grupoName, Usuario miembro) {
        boolean removed = false;
        Optional<Grupo> grupoOptional = this.grupos.getGrupoByName(grupoName);
        if (grupoOptional.isPresent()) {
            Grupo grupo = grupoOptional.get();
            if (grupo.getMembers().contains(miembro)) {
                grupo.getMembers().remove(miembro);
                miembro.getGrupos().remove(grupo);
                this.miembros.saveUsuario(miembro);
                removed = true;
            }
            if (grupo.getMembers().isEmpty()) {
                this.deleteGrupo(grupo);
            }
        }
        return removed;
    }

    public boolean isMemberIn(String grupoName, Usuario miembro) {
        Optional<Grupo> grupoOptional = this.grupos.getGrupoByName(grupoName);
        return grupoOptional.isPresent() && grupoOptional.get().getMembers().contains(miembro);
    }

    public void deleteGrupo(Grupo grupo) {
        for (Usuario member : grupo.getMembers()) {
            member.getGrupos().remove(grupo);
            grupo.getMembers().remove(member);
            this.miembros.saveUsuario(member);
        }
        this.grupos.delete(grupo);
    }

    public boolean isNameUsed(String name) {
        return this.grupos.countByName(name) > 0;
    }
}
