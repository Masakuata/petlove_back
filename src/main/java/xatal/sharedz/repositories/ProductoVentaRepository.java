package xatal.sharedz.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import xatal.sharedz.entities.ProductoVenta;

@Repository
public interface ProductoVentaRepository extends CrudRepository<ProductoVenta, Long> {
}
