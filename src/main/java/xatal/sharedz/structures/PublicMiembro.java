package xatal.sharedz.structures;

import xatal.sharedz.entities.Miembro;

public class PublicMiembro {
    public String username;
    public String email;

    public PublicMiembro() {
    }

    public PublicMiembro(Miembro miembro) {
        this.username = miembro.getUsername();
        this.email = miembro.getEmail();
    }

}
