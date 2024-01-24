package xatal.petlove.repositories;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import xatal.petlove.entities.Direccion;

public interface DireccionRepository extends CrudRepository<Direccion, Long> {
	@Modifying
	@Transactional
	@Query(value = "UPDATE petlove.direccion SET direccion = :direccion WHERE id = :id",
		nativeQuery = true)
	void updateDireccionById(long id, String direccion);

	@Query(value = "SELECT COUNT(*) FROM petlove.venta WHERE direccion = :id",
		nativeQuery = true)
	long isReferenced(long id);

	@Modifying
	@Transactional
	@Query(value = "UPDATE direccion SET status = 0 WHERE id = :idDireccion",
		nativeQuery = true)
	void deactivateDireccion(long idDireccion);
}
