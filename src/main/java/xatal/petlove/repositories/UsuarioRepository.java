package xatal.petlove.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import xatal.petlove.entities.Usuario;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends CrudRepository<Usuario, Long> {
    @Query(value = "SELECT * FROM Sharedz.usuario WHERE status = true",
            nativeQuery = true)
    List<Usuario> getAll();

    Optional<Usuario> findByEmailAndPassword(String email, String password);

    Optional<Usuario> getUsuarioByEmailAndPassword(String email, String password);

    Optional<Usuario> findOneByEmail(String email);

    Optional<Usuario> findUsuarioByUsername(String username);

    Long deleteByEmail(String email);

    Long countByEmail(String email);

    Long countByUsername(String username);

    Long countByEmailAndUsername(String email, String username);
}
