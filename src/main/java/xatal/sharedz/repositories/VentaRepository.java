package xatal.sharedz.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import xatal.sharedz.entities.Venta;

import java.util.List;
import java.util.Optional;

public interface VentaRepository extends CrudRepository<Venta, Long> {
    @Query(value = "SELECT * FROM Sharedz.venta",
            nativeQuery = true)
    List<Venta> getAll();

    Optional<Venta> getById(Long id);

    Long countById(Long id);
}
