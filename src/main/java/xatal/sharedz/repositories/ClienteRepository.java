package xatal.sharedz.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import xatal.sharedz.entities.Cliente;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClienteRepository extends CrudRepository<Cliente, Long> {
    @Query(value = "SELECT * FROM cliente",
            nativeQuery = true)
    List<Cliente> getAll();

    Optional<Cliente> getById(Long id);

    Long countByEmail(String email);

    Long countById(Long id);

    void deleteById(Long id);
}
