package xatal.sharedz.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import xatal.sharedz.entities.Miembro;

import java.util.List;
import java.util.Optional;

@Repository
public interface MiembroRepository extends CrudRepository<Miembro, Long> {
    @Query(value = "SELECT * FROM Sharedz.miembro WHERE status = true",
            nativeQuery = true)
    List<Miembro> getAll();

    @Query(value = "SELECT * FROM Sharedz.miembro WHERE email = :email AND password = :password",
            nativeQuery = true)
    Optional<Miembro> login(
            @Param("email") String email,
            @Param("password") String password);

    Optional<Miembro> findOneByEmail(String email);

    Optional<Miembro> findMiembroByUsername(String username);

    Long deleteByEmail(String email);

    Long countByEmail(String email);

    Long countByUsername(String username);

    Long countByEmailAndUsername(String email, String username);
}
