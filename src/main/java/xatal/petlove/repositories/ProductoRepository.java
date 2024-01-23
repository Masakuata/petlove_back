package xatal.petlove.repositories;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import xatal.petlove.entities.Producto;

import java.util.List;
import java.util.Optional;

public interface ProductoRepository extends CrudRepository<Producto, Long>, JpaSpecificationExecutor<Producto> {
	@Query(value = "SELECT * FROM Sharedz.producto WHERE status = 1",
		nativeQuery = true)
	List<Producto> getAll();

	Optional<Producto> getProductoByNombre(String nombre);

	@Query(value = "SELECT * FROM producto WHERE id IN :ids",
		nativeQuery = true)
	List<Producto> findByIdIn(List<Long> ids);

	@Modifying
	@Query(value = "UPDATE producto SET cantidad = (cantidad + :retorno) WHERE id = :idProducto",
		nativeQuery = true)
	void returnStock(long idProducto, int retorno);

	Long countById(long id);

	Long countByNombreAndPresentacion(String nombre, String presentacion);

	void deleteById(int id);

	@Modifying
	@Query(value = "UPDATE producto SET status = 0 WHERE id = :idProducto",
		nativeQuery = true)
	void deactivateProducto(int idProducto);
}
