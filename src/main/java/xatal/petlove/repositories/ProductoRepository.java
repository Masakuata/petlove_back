package xatal.petlove.repositories;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import xatal.petlove.entities.Producto;

import java.util.List;
import java.util.Optional;

public interface ProductoRepository extends CrudRepository<Producto, Long>, JpaSpecificationExecutor<Producto> {
	@Query(value = "SELECT * FROM Sharedz.producto",
		nativeQuery = true)
	List<Producto> getAll();

	Optional<Producto> getProductoByNombre(String nombre);

	@Query(value = "SELECT * FROM producto WHERE id IN :ids",
		nativeQuery = true)
	List<Producto> findByIdIn(List<Long> ids);

	@Query(value = "UPDATE producto SET cantidad = (cantidad + :retorno) WHERE id = :idProducto",
		nativeQuery = true)
	void returnStock(long idProducto, int retorno);

	Long countById(int id);

	void deleteById(int id);
}
