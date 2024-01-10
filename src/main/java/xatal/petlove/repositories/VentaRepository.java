package xatal.petlove.repositories;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import xatal.petlove.entities.Venta;
import xatal.petlove.structures.PublicVenta;

import java.util.List;
import java.util.Optional;

public interface VentaRepository extends CrudRepository<Venta, Long>, JpaSpecificationExecutor<Venta> {
	@Query(value = "SELECT * FROM Sharedz.venta",
		nativeQuery = true)
	List<Venta> getAll();

	Optional<Venta> getById(Long id);

	Long countById(Long id);

	@Query(value = "SELECT id, facturado, fecha, pagado, cliente, abonado  FROM venta " +
		"INNER JOIN (SELECT venta, SUM(cantidad) AS abonado FROM abono GROUP BY venta) AS AbonosVenta " +
		"ON venta.id = AbonosVenta.venta",
		nativeQuery = true)
	List<PublicVenta> getPublicVentas();
}
