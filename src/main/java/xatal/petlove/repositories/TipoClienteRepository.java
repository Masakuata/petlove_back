package xatal.petlove.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import xatal.petlove.entities.TipoCliente;

import java.util.List;

public interface TipoClienteRepository extends CrudRepository<TipoCliente, Long> {
	@Query(value = "SELECT * FROM tipo_cliente",
		nativeQuery = true)
	List<TipoCliente> getAll();
}
