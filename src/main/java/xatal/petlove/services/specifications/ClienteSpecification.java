package xatal.petlove.services.specifications;

import org.springframework.data.jpa.domain.Specification;
import xatal.petlove.entities.Cliente;

public abstract class ClienteSpecification {
	public static Specification<Cliente> filterById(Integer id) {
		if (id != null) {
			return (root, query, builder) -> builder.equal(root.get("id"), id);
		}
		return Specification.where(null);
	}

	public static Specification<Cliente> filterByName(String nombre) {
		if (nombre != null && !nombre.isEmpty()) {
			return (root, query, builder) -> builder.like(
				builder.lower(root.get("nombre")),
				"%" + nombre.toLowerCase() + "%"
			);
		}
		return Specification.where(null);
	}
}
