package xatal.petlove.services;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import xatal.petlove.entities.Venta;
import xatal.petlove.repositories.VentaRepository;
import xatal.petlove.services.specifications.VentaSpecification;

import java.util.List;
import java.util.Optional;

@Service
public class SearchVentaService {
	private final VentaRepository ventaRepository;

	public SearchVentaService(VentaRepository ventaRepository) {
		this.ventaRepository = ventaRepository;
	}

	public List<Venta> searchVentas(
		Integer idCliente,
		String nombreCliente,
		Integer producto,
		Integer year,
		Integer month,
		Integer day,
		Boolean pagado,
		Float abono,
		Integer size,
		Integer pag
	) {
		Specification<Venta> spec = Specification.allOf(
			VentaSpecification.filterByIdCliente(idCliente),
			VentaSpecification.filterByNombreCliente(nombreCliente),
			VentaSpecification.filterByDay(day),
			VentaSpecification.filterByMonth(month),
			VentaSpecification.filterByYear(year),
			VentaSpecification.filterPagado(pagado),
			VentaSpecification.filterAbonado(abono),
			VentaSpecification.orderByNewer()
		);
		List<Venta> ventas;
		if (pag != null && size != null) {
			Pageable pageable = PageRequest.of(pag, size);
			ventas = new java.util.ArrayList<>(this.ventaRepository.findAll(spec, pageable).stream().toList());
		} else {
			ventas = new java.util.ArrayList<>(this.ventaRepository.findAll(spec).stream().toList());
		}
		if (producto != null) {
			return VentaSpecification.filterByProducto(ventas, Long.valueOf(producto));
		}
		return ventas;
	}

	public List<Venta> getAll() {
		return this.ventaRepository.getAll();
	}

	public Optional<Venta> getById(long id) {
		return this.ventaRepository.getById(id);
	}
}
