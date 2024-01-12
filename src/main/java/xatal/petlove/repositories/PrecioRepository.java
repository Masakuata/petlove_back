package xatal.petlove.repositories;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import xatal.petlove.entities.Precio;
import xatal.petlove.entities.Venta;

import java.util.List;
import java.util.Optional;

public interface PrecioRepository extends CrudRepository<Precio, Long>, JpaSpecificationExecutor<Precio> {

    @Query(value = "SELECT * FROM precio",
            nativeQuery = true)
    List<Precio> getAll();

    List<Precio> findByProductoInAndCliente(List<Long> productos, Long cliente);

    Optional<Precio> findByProductoAndCliente(Long producto, Long cliente);

    List<Precio> findByProducto(Long producto);
}
