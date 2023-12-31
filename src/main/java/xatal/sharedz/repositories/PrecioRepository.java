package xatal.sharedz.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import xatal.sharedz.entities.Precio;

import java.util.List;
import java.util.Optional;

public interface PrecioRepository extends CrudRepository<Precio, Long> {

    @Query(value = "SELECT * FROM precio",
            nativeQuery = true)
    List<Precio> getAll();

    List<Precio> findByProductoInAndCliente(List<Long> productos, Long cliente);

    Optional<Precio> findByProductoAndCliente(Long producto, Long cliente);

    List<Precio> findByProducto(Long producto);
}
