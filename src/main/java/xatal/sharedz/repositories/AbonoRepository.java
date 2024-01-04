package xatal.sharedz.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import xatal.sharedz.entities.Abono;

import java.util.List;

public interface AbonoRepository extends CrudRepository<Abono, Long> {
    List<Abono> findByVenta(Long venta);

    Long countById(Long id);

    @Query(value = "SELECT SUM(cantidad) FROM abono WHERE venta = :venta",
            nativeQuery = true)
    Float sumAbonosByVenta(Long venta);
}
