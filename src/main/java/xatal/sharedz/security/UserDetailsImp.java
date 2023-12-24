package xatal.sharedz.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import xatal.sharedz.entities.Miembro;

import java.util.Collection;
import java.util.Collections;

public class UserDetailsImp implements UserDetails {
    private final Miembro miembro;

    public UserDetailsImp(Miembro miembro) {
        this.miembro = miembro;
    }

    public Miembro getMiembro() {
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
