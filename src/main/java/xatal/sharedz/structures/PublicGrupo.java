package xatal.sharedz.structures;

import xatal.sharedz.entities.Grupo;

import java.util.HashSet;
import java.util.Set;

public class PublicGrupo {
    public String name;

    public Set<String> members = new HashSet<>();

    public PublicGrupo(Grupo grupo) {
        this.name = grupo.getName();
        grupo.getMembers().forEach(member -> this.members.add(member.getUsername()));
    }
}
