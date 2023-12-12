package xatal.sharedz.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import xatal.sharedz.entities.Miembro;
import xatal.sharedz.repositories.MiembroRepository;

@Service
public class UserDetailServiceImp implements UserDetailsService {
    final MiembroRepository miembroRepository;

    public UserDetailServiceImp(MiembroRepository miembroRepository) {
        this.miembroRepository = miembroRepository;
    }


    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Miembro miembro = miembroRepository
                .findOneByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(""));
        return new UserDetailsImp(miembro);
    }
}
