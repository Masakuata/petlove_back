package xatal.petlove.services;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import xatal.petlove.entities.Producto;
import xatal.petlove.repositories.ProductoRepository;
import xatal.petlove.services.specifications.ProductoSpecification;

import java.util.List;
import java.util.Optional;

@Service
public class SearchProductoService {
	private final ProductoRepository productoRepository;
	private final PrecioProductoService precioProductoService;

	public SearchProductoService(ProductoRepository productoRepository, PrecioProductoService precioProductoService) {
		this.productoRepository = productoRepository;
		this.precioProductoService = precioProductoService;
	}

	//	@Cacheable("searchProducto")
	public List<Producto> search(
		Integer idProducto,
		String nombre,
		Integer tipoCliente,
		Integer size,
		Integer pag
	) {
		Specification<Producto> spec = Specification.allOf(
			ProductoSpecification.searchId(idProducto),
			ProductoSpecification.searchNombre(nombre),
			ProductoSpecification.searchActive(),
			ProductoSpecification.orderByName()
		);
		Pageable pageable = PageRequest.of(pag, size);
		List<Producto> productos = this.productoRepository.findAll(spec, pageable).stream().toList();
		if (tipoCliente != null) {
			this.precioProductoService.setProductosPrices(productos, tipoCliente.longValue());
		}
		return productos;
	}

	public Optional<Producto> searchProductoById(long idProducto) {
		return this.productoRepository.findById(idProducto);
	}

	public List<Producto> searchByIdsAndTipoCliente(List<Integer> ids, int tipoCliente) {
		List<Producto> productos = this.productoRepository.findByIdIn(ids.stream().map(Integer::longValue).toList());
		this.precioProductoService.setProductosPrices(productos, tipoCliente);
		return productos;
	}
}
