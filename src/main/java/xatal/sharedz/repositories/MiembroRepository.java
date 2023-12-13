package xatal.sharedz.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import xatal.sharedz.entities.Miembro;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

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

    void deleteMiembroByEmail(String email);
}
