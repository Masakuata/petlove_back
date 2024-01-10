package xatal.petlove.structures;

import xatal.petlove.entities.Usuario;

public class PublicUsuario {
    public String username;
    public String email;

    public PublicUsuario() {
    }

    public PublicUsuario(Usuario miembro) {
        this.username = miembro.getUsername();
        this.email = miembro.getEmail();
    }

}
