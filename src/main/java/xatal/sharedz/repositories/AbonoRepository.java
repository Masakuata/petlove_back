package xatal.sharedz.repositories;

import org.springframework.data.repository.CrudRepository;
import xatal.sharedz.entities.Abono;

import java.util.List;

public interface AbonoRepository extends CrudRepository<Abono, Long> {
    List<Abono> findByVenta(Long venta);

    Long countById(Long id);
}
