package xatal.sharedz.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import xatal.sharedz.entities.Cliente;
import xatal.sharedz.structures.ClienteMinimal;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ClienteRepository extends CrudRepository<Cliente, Long> {
    @Query(value = "SELECT * FROM cliente",
            nativeQuery = true)
    List<Cliente> getAll();

    @Query(value = "SELECT id, nombre, tipo_cliente, RFC FROM cliente",
            nativeQuery = true)
    Collection<ClienteMinimal> getMinimal();

    Optional<Cliente> getById(Long id);

    Long countByEmail(String email);

    Long countById(Long id);

    void deleteById(Long id);
}
