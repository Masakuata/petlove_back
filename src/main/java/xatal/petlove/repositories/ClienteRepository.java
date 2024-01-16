package xatal.petlove.repositories;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import xatal.petlove.entities.Cliente;
import xatal.petlove.structures.ClienteMinimal;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ClienteRepository extends CrudRepository<Cliente, Long>, JpaSpecificationExecutor<Cliente> {
    @Query(value = "SELECT * FROM cliente",
            nativeQuery = true)
    List<Cliente> getAll();

    @Query(value = "SELECT id, nombre, tipo_cliente, RFC, telefono, email FROM cliente",
            nativeQuery = true)
    Collection<ClienteMinimal> getMinimal();

    Optional<Cliente> getById(Long id);

    Long countByEmail(String email);

    Long countById(Long id);

    void deleteById(Long id);
}
