package xatal.sharedz.repositories;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import xatal.sharedz.entities.Abono;
import xatal.sharedz.structures.PublicVenta;

import java.util.List;
import java.util.Map;

public interface AbonoRepository extends CrudRepository<Abono, Long>, JpaSpecificationExecutor<Abono> {
	List<Abono> findByVenta(Long venta);

	Long countById(Long id);

	@Query(value = "SELECT SUM(cantidad) FROM abono WHERE venta = :venta",
		nativeQuery = true)
	Float sumAbonosByVenta(Long venta);

	@Query(value = "SELECT a.venta, SUM(a.cantidad) FROM Abono a WHERE a.venta IN :ventas GROUP BY a.venta")
	Map<Long, Float> sumAbonosByVentas(List<Long> ventas);

	@Query(value = "UPDATE abono SET cantidad = :cantidad WHERE id = :idAbono",
		nativeQuery = true)
	void updateAbonoCantidad(Long idAbono, float cantidad);
}
