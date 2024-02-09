package xatal.petlove.services.specifications;

import org.springframework.data.jpa.domain.Specification;
import xatal.petlove.entities.Producto;

import java.util.List;

public abstract class ProductoSpecification {
	public static Specification<Producto> filterByProductIds(List<Long> productosId) {
		return (root, query, builder) -> builder.in(root.get("id")).value(productosId);
	}

	public static Specification<Producto> searchId(Integer id) {
		if (id != null) {
			return (root, query, builder) -> builder.equal(root.get("id"), id);
		}
		return Specification.where(null);
	}

	public static Specification<Producto> searchNombre(String nombre) {
		if (nombre != null && !nombre.isEmpty()) {
			return (root, query, builder) ->
				builder.like(
					builder.lower(root.get("nombre")),
					"%" + nombre.toLowerCase() + "%"
				);
		}
		return Specification.where(null);
	}

	public static Specification<Producto> searchActive() {
		return (root, query, builder) -> builder.isTrue(root.get("status"));
	}
}
