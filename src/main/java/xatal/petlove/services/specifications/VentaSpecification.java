package xatal.petlove.services.specifications;

import org.springframework.data.jpa.domain.Specification;
import xatal.petlove.entities.Venta;

public abstract class VentaSpecification {
	public static Specification<Venta> filterByIdCliente(Integer id) {
		if (id != null) {
			return (root, query, builder) -> builder.equal(root.get("cliente").get("id"), id);
		}
		return Specification.where(null);
	}

	public static Specification<Venta> filterByNombreCliente(String nombreCliente) {
		if (nombreCliente != null && !nombreCliente.isEmpty()) {
			return (root, query, builder) -> builder.like(
				builder.lower(root.get("cliente").get("nombre")),
				"%" + nombreCliente.toLowerCase() + "%"
			);
		}
		return Specification.where(null);
	}

	public static Specification<Venta> filterByDay(Integer day) {
		if (day != null) {
			return (root, query, builder) -> builder.equal(
				builder.function("day", Integer.class, root.get("fecha")),
				day
			);
		}
		return Specification.where(null);
	}

	public static Specification<Venta> filterByMonth(Integer month) {
		if (month != null) {
			return (root, query, builder) -> builder.equal(
				builder.function("month", Integer.class, root.get("fecha")),
				month
			);
		}
		return Specification.where(null);
	}

	public static Specification<Venta> filterByYear(Integer year) {
		if (year != null) {
			return (root, query, builder) -> builder.equal(
				builder.function("year", Integer.class, root.get("fecha")),
				year
			);
		}
		return Specification.where(null);
	}

	public static Specification<Venta> filterPagado(Boolean pagado) {
		if (pagado != null) {
			return (root, query, builder) -> builder.equal(root.get("pagado"), pagado);
		}
		return Specification.where(null);
	}

	public static Specification<Venta> orderByNewer() {
		return (root, query, builder) -> query.orderBy(builder.desc(root.get("fecha"))).getRestriction();
	}
}
