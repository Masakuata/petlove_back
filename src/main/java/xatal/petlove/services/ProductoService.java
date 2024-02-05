package xatal.petlove.services;

import jakarta.transaction.Transactional;
import org.antlr.v4.runtime.misc.Pair;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import xatal.petlove.entities.Precio;
import xatal.petlove.entities.Producto;
import xatal.petlove.entities.ProductoVenta;
import xatal.petlove.entities.StockOperation;
import xatal.petlove.entities.Venta;
import xatal.petlove.mappers.PrecioMapper;
import xatal.petlove.mappers.TipoClienteMapper;
import xatal.petlove.repositories.PrecioRepository;
import xatal.petlove.repositories.ProductoRepository;
import xatal.petlove.repositories.ProductoVentaRepository;
import xatal.petlove.repositories.StockOperationRepository;
import xatal.petlove.services.specifications.ProductoSpecification;
import xatal.petlove.structures.MultiDetailedPrecioProducto;
import xatal.petlove.structures.MultiPrecioProducto;
import xatal.petlove.structures.ProductoLoad;
import xatal.petlove.structures.PublicPrecio;
import xatal.petlove.structures.PublicProducto;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductoService {
	private final ProductoRepository productoRepository;
	private final ProductoVentaRepository productoVenta;
	private final SearchProductoService searchProductoService;
	private final PrecioRepository precioRepository;
	private final PrecioMapper precioMapper;
	private final StockOperationRepository stockOperationRepository;

	public ProductoService(ProductoRepository productoRepository, ProductoVentaRepository productoVenta,
	                       SearchProductoService searchProductoService, PrecioRepository precioRepository,
	                       PrecioMapper precioMapper, StockOperationRepository stockOperationRepository) {
		this.productoRepository = productoRepository;
		this.productoVenta = productoVenta;
		this.searchProductoService = searchProductoService;
		this.precioRepository = precioRepository;
		this.precioMapper = precioMapper;
		this.stockOperationRepository = stockOperationRepository;
	}

	public void cargarProductos(List<ProductoLoad> newProductos) {
		List<Precio> precios = new LinkedList<>();

		for (ProductoLoad newProducto : newProductos) {
			Producto aux = new Producto();
			aux.setNombre(newProducto.nombre);
			aux.setPresentacion(newProducto.presentacion);
			aux.setTipoMascota(newProducto.tipoMascota);
			aux.setRaza(newProducto.raza);
			aux.setPrecio(newProducto.precioDefecto);
			aux.setCantidad(newProducto.cantidad);
			aux = this.productoRepository.save(aux);
			for (PublicPrecio precio : newProducto.precios) {
				Precio precioAux = new Precio();
				precioAux.setCliente((long) precio.id);
				precioAux.setProducto(aux.getId());
				precioAux.setPrecio(precio.precio);
				precios.add(precioAux);
			}
		}
		this.precioRepository.saveAll(precios);
	}

	public List<MultiPrecioProducto> getWithPrecios() {
		Map<Long, MultiPrecioProducto> multiPrecioMap = this.productoRepository.getAll()
			.stream()
			.map(producto -> new Pair<>(producto.getId(), new MultiPrecioProducto(producto)))
			.collect(Collectors.toMap(pair -> pair.a, pair -> pair.b));

		this.precioRepository.getAll().forEach(precio -> {
			if (multiPrecioMap.containsKey(precio.getProducto())) {
				multiPrecioMap.get(precio.getProducto()).precios.add(new PublicPrecio(precio));
			}
		});
		return multiPrecioMap.values().stream().toList();
	}

	public Optional<MultiPrecioProducto> getWithPreciosById(long idProducto) {
		Optional<Producto> optionalProducto = this.searchProductoService.searchProductoById(idProducto);
		if (optionalProducto.isEmpty()) {
			return Optional.empty();
		}
		List<PublicPrecio> precios = this.precioRepository.findByProducto(idProducto)
			.stream()
			.map(PublicPrecio::new)
			.toList();
		MultiPrecioProducto producto = new MultiPrecioProducto(optionalProducto.get());
		producto.precios = precios;
		return Optional.of(producto);
	}

	public Optional<MultiDetailedPrecioProducto> getWithPreciosAndTipoClienteById(long idProducto) {
		Optional<MultiPrecioProducto> optionalProducto = this.getWithPreciosById(idProducto);
		if (optionalProducto.isEmpty()) {
			return Optional.empty();
		}
		MultiDetailedPrecioProducto producto = new MultiDetailedPrecioProducto(optionalProducto.get());
		producto.precios = this.precioMapper.publicToDetailed(optionalProducto.get().precios);
		return Optional.of(producto);
	}

	public void setProductoPrecio(Producto producto, long tipoCliente) {
		Specification<Precio> spec = Specification.where(((root, query, builder) ->
			builder.and(
				builder.equal(root.get("cliente"), tipoCliente),
				builder.equal(root.get("producto"), producto.getId())))
		);
		this.precioRepository.findAll(spec)
			.stream()
			.findFirst()
			.ifPresent(precio -> producto.setPrecio(precio.getPrecio()));
	}

	public void setProductosPrices(List<Producto> productos, long tipoCliente) {
		List<Long> idProductos = productos.stream().map(Producto::getId).toList();
		Map<Long, Float> precios = this.precioRepository.findByProductoInAndCliente(idProductos, tipoCliente)
			.stream()
			.collect(Collectors.toMap(Precio::getProducto, Precio::getPrecio));

		productos.forEach(producto -> {
			if (precios.containsKey(producto.getId())) {
				producto.setPrecio(precios.get(producto.getId()));
			}
		});
	}

	private void updatePrecios(Map<Integer, Precio> preciosMap, List<PublicPrecio> newPrecios, int idProducto) {
		newPrecios.forEach(newPrecio -> {
			if (preciosMap.containsKey(newPrecio.id)) {
				preciosMap.get(newPrecio.id).setPrecio(newPrecio.precio);
			} else {
				preciosMap.put(newPrecio.id, new Precio(idProducto, newPrecio.id, newPrecio.precio));
			}
		});
	}

	public boolean savePreciosById(int idProducto, List<PublicPrecio> newPrecios) {
		if (!this.isIdRegistered(idProducto)) {
			return false;
		}
		Map<Integer, Precio> savedPreciosMap = TipoClienteMapper.mapTipoClientePrecio(
			this.precioRepository.findByProducto((long) idProducto));

		this.updatePrecios(savedPreciosMap, newPrecios, idProducto);
		this.precioRepository.saveAll(savedPreciosMap.values().stream().toList());
		return true;
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
		this.savePreciosById(Math.toIntExact(savedProducto.getId()), newProducto.precios);
		return savedProducto;
	}

	@Transactional
	public MultiPrecioProducto updateProducto(MultiPrecioProducto newProducto, long idProducto) {
		Producto aux = new Producto(newProducto);
		aux.setId(idProducto);
		this.productoRepository.save(aux);
		this.precioRepository.deleteByProducto(idProducto);
		this.savePreciosById((int) idProducto, newProducto.precios);
		return newProducto;
	}

	public Producto saveProducto(Producto producto) {
		return this.productoRepository.save(producto);
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

	public boolean isReferenced(int idProducto) {
		return this.productoVenta.countByProducto((long) idProducto) > 0;
	}

	@Transactional
	public boolean deleteById(int idProducto) {
		if (!this.isReferenced(idProducto)) {
			this.productoRepository.deleteById(idProducto);
			return true;
		}
		return false;
		// TODO Check if producto is used on another table
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
