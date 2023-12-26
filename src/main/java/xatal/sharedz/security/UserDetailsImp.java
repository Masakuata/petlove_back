package xatal.sharedz.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import xatal.sharedz.entities.Usuario;

import java.util.Collection;
import java.util.Collections;

public class UserDetailsImp implements UserDetails {
    private final Usuario miembro;

    public UserDetailsImp(Usuario miembro) {
        this.miembro = miembro;
    }

    public Usuario getMiembro() {
        return this.miembro;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList();
    }

    @Override
    public String getPassword() {
        return miembro.getPassword();
    }

    @Override
    public String getUsername() {
        return miembro.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

   public String getName() {
       return miembro.getUsername();
   }
}
