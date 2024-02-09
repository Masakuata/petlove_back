package xatal.petlove.services.specifications;

import org.springframework.data.jpa.domain.Specification;
import xatal.petlove.entities.Precio;

public abstract class PrecioSpecification {
	public static Specification<Precio> filterByTipoCliente(Long tipoCliente) {
		if (tipoCliente != null) {
			return (root, query, builder) -> builder.equal(
				root.get("cliente"),
				tipoCliente
			);
		}
		return Specification.where(null);
	}

	public static Specification<Precio> filterByProductoId(Long idProducto) {
		if (idProducto != null) {
			return (root, query, builder) -> builder.equal(
				root.get("producto"),
				idProducto
			);
		}
		return Specification.where(null);
	}
}
