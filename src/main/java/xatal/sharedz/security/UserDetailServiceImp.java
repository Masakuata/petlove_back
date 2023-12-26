package xatal.sharedz.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import xatal.sharedz.entities.Usuario;
import xatal.sharedz.repositories.UsuarioRepository;

@Service
public class UserDetailServiceImp implements UserDetailsService {
    final UsuarioRepository miembroRepository;

    public UserDetailServiceImp(UsuarioRepository miembroRepository) {
        this.miembroRepository = miembroRepository;
    }


    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario miembro = miembroRepository
                .findOneByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(""));
        return new UserDetailsImp(miembro);
    }
}
