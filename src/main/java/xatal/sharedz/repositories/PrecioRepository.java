package xatal.sharedz.repositories;

import org.springframework.data.repository.CrudRepository;
import xatal.sharedz.entities.Precio;

import java.util.List;

public interface PrecioRepository extends CrudRepository<Precio, Long> {
    List<Precio> findByProductoInAndCliente(List<Long> productos, Long cliente);

    List<Precio> findByProducto(Long producto);
}
