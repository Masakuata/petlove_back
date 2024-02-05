package xatal.petlove.services;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import xatal.petlove.entities.Precio;
import xatal.petlove.entities.Producto;
import xatal.petlove.entities.ProductoVenta;
import xatal.petlove.entities.StockOperation;
import xatal.petlove.entities.Venta;
import xatal.petlove.repositories.PrecioRepository;
import xatal.petlove.repositories.ProductoRepository;
import xatal.petlove.repositories.StockOperationRepository;
import xatal.petlove.services.specifications.PrecioSpecification;
import xatal.petlove.services.specifications.ProductoSpecification;
import xatal.petlove.structures.MultiPrecioProducto;
import xatal.petlove.structures.PublicProducto;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductoService {
	private final ProductoRepository productoRepository;
	private final PrecioProductoService precioProductoService;
	private final SearchProductoService searchProductoService;
	private final PrecioRepository precioRepository;
	private final StockOperationRepository stockOperationRepository;

	public ProductoService(ProductoRepository productoRepository, PrecioProductoService precioProductoService,
	                       SearchProductoService searchProductoService, PrecioRepository precioRepository,
	                       StockOperationRepository stockOperationRepository) {
		this.productoRepository = productoRepository;
		this.precioProductoService = precioProductoService;
		this.searchProductoService = searchProductoService;
		this.precioRepository = precioRepository;
		this.stockOperationRepository = stockOperationRepository;
	}

	public void setProductoPrecio(Producto producto, long tipoCliente) {
		Specification<Precio> filters = Specification.allOf(
			PrecioSpecification.filterByProductoId(producto.getId()),
			PrecioSpecification.findByTipoCliente(tipoCliente)
		);
		this.precioRepository.findAll(filters)
			.stream()
			.findFirst()
			.ifPresent(precio -> producto.setPrecio(precio.getPrecio()));
	}

	public void updateStockFromVenta(Venta venta) {
		List<Producto> updated = new LinkedList<>();
		venta.getProductos().forEach(productoVenta ->
			this.updateProductoQuantity(productoVenta).ifPresent(updated::add));
		this.productoRepository.saveAll(updated);
	}

	@Transactional
	public void returnStock(long idProducto, int cantidad) {
		this.productoRepository.returnStock(idProducto, cantidad);
		this.stockOperationRepository.save(new StockOperation(idProducto, cantidad));
	}

	public Producto saveProducto(MultiPrecioProducto newProducto) {
		Producto savedProducto = this.productoRepository.save(new Producto(newProducto));
		this.precioProductoService.savePreciosById(Math.toIntExact(savedProducto.getId()), newProducto.precios);
		return savedProducto;
	}

	@Transactional
	public MultiPrecioProducto updateProducto(MultiPrecioProducto newProducto, long idProducto) {
		Producto aux = new Producto(newProducto);
		aux.setId(idProducto);
		this.productoRepository.save(aux);
		this.precioRepository.deleteByProducto(idProducto);
		this.precioProductoService.savePreciosById((int) idProducto, newProducto.precios);
		return newProducto;
	}

	public Map<Long, Integer> getStockByProductos(List<Long> idProductos) {
		return this.productoRepository.findAll(ProductoSpecification.productoInIds(idProductos))
			.stream()
			.collect(Collectors.toMap(Producto::getId, Producto::getCantidad));
	}

	@Transactional
	public void updateStockWithDelta(long idProducto, int stockDelta) {
		this.productoRepository.returnStock(idProducto, stockDelta);

	}

	public boolean isProductoRegistered(PublicProducto producto) {
		return this.isProductoRegistered(new Producto(producto));
	}

	public boolean isProductoRegistered(Producto producto) {
		return this.productoRepository.countByNombreAndPresentacion(producto.getNombre(), producto.getPresentacion()) > 0;
	}

	public boolean isIdRegistered(long idProducto) {
		return this.productoRepository.countById(idProducto) > 0;
	}

	@Transactional
	public void deactivateProducto(int idProducto) {
		this.productoRepository.deactivateProducto(idProducto);
	}

	private Optional<Producto> updateProductoQuantity(ProductoVenta productoVenta) {
		return this.searchProductoService.searchProductoById(Math.toIntExact(productoVenta.getProducto()))
			.map(producto -> {
				producto.setCantidad(producto.getCantidad() - productoVenta.getCantidad());
				return producto;
			});
	}

	public float getPesoVenta(Venta venta) {
		List<Long> ids = venta.getProductos()
			.stream()
			.map(ProductoVenta::getId)
			.toList();
		return this.productoRepository.findByIdIn(ids)
			.stream()
			.map(Producto::getPeso)
			.reduce(0F, Float::sum);
	}
}
