package xatal.sharedz.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import xatal.sharedz.entities.Cliente;

import java.util.List;

@Repository
public interface ClienteRepository extends CrudRepository<Cliente, Long> {
    @Query(value = "SELECT * FROM Sharedz.cliente",
            nativeQuery = true)
    List<Cliente> getAll();

    Long countByEmail(String email);

    Long countById(Long id);

    void deleteById(Long id);
}
