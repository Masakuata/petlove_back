package xatal.petlove.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import xatal.petlove.entities.ProductoVenta;

import java.util.List;

@Repository
public interface ProductoVentaRepository extends CrudRepository<ProductoVenta, Long> {
    Long countByProducto(Long producto);

    List<ProductoVenta> findByProducto(Long idProducto);
}
