package xatal.sharedz.services;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import xatal.sharedz.entities.Precio;
import xatal.sharedz.entities.Producto;
import xatal.sharedz.entities.ProductoVenta;
import xatal.sharedz.entities.Venta;
import xatal.sharedz.repositories.PrecioRepository;
import xatal.sharedz.repositories.ProductoRepository;
import xatal.sharedz.repositories.ProductoVentaRepository;
import xatal.sharedz.structures.PublicPrecio;
import xatal.sharedz.structures.PublicProducto;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductoService {
	private final ProductoRepository productos;
	private final ProductoVentaRepository productoVenta;
	private final PrecioRepository precioRepository;

	private List<Producto> productosCache = null;
	private List<Precio> preciosCache = null;

	public ProductoService(ProductoRepository productos, ProductoVentaRepository productoVenta, PrecioRepository precioRepository) {
		this.productos = productos;
		this.productoVenta = productoVenta;
		this.precioRepository = precioRepository;
		this.ensureProductosCacheLoaded();
		this.ensurePreciosCacheLoaded();
	}

	private void ensureProductosCacheLoaded() {
		if (this.productosCache == null) {
			this.productosCache = this.productos.getAll();
		}
	}

	private void ensurePreciosCacheLoaded() {
		if (this.preciosCache == null) {
			this.preciosCache = this.precioRepository.getAll();
		}
	}

	public List<Producto> getAll() {
		this.ensureProductosCacheLoaded();
		return this.productosCache;
	}

	public List<Producto> searchByName(String nombre) {
		String lowercase = nombre.toLowerCase();
		return this.productos.getAll()
			.stream()
			.filter(producto -> producto.getNombre().toLowerCase().contains(lowercase))
			.collect(Collectors.toList());
	}

	public List<Producto> searchByNameAndTipoCliente(String nombre, int tipoCliente) {
		List<Producto> productos = this.searchByName(nombre);
		if (productos.isEmpty()) {
			return Collections.emptyList();
		}

		List<Precio> precios = this.precioRepository.findByProductoInAndCliente(
			this.getProductosId(productos), (long) tipoCliente
		);

		Map<Long, Precio> precioMap = precios
			.stream()
			.collect(Collectors.toMap(Precio::getProducto, precio -> precio));

		productos.forEach(producto -> {
			if (precioMap.containsKey(producto.getId())) {
				producto.setPrecio(precioMap.get(producto.getId()).getPrecio());
			}
		});
		return productos;
	}

	public List<Producto> searchByIdsAndTipoCliente(List<Integer> ids, int tipoCliente) {
		List<Producto> productos = this.productos.findByIdIn(ids.stream().map(Integer::longValue).toList());
		this.setProductosPrices(productos, tipoCliente);
		return productos;
	}

	public Optional<Producto> getByIdAndTipoCliente(int idProducto, int tipoCliente) {
		Optional<Producto> producto = this.getProductoById(idProducto);
		producto.ifPresent(currentProducto -> this.setProductPrice(currentProducto, tipoCliente));
		return producto;
	}

	public void setProductPrice(Producto producto, int tipoCliente) {
		this.ensureProductosCacheLoaded();
		this.preciosCache
			.stream()
			.filter(precio -> precio.getCliente() == tipoCliente)
			.findFirst()
			.ifPresent(precio -> producto.setPrecio(precio.getPrecio()));
	}

	public void setProductosPrices(List<Producto> productos, int tipoCliente) {
		List<Long> idProductos = productos.stream().map(Producto::getId).toList();
		Map<Long, Float> precios = this.precioRepository.findByProductoInAndCliente(idProductos, (long) tipoCliente)
			.stream()
			.collect(Collectors.toMap(Precio::getProducto, Precio::getPrecio));

		productos.forEach(producto -> {
			if (precios.containsKey(producto.getId())) {
				producto.setPrecio(precios.get(producto.getId()));
			}
		});
	}

	public boolean setPreciosById(int idProducto, List<PublicPrecio> newPrecios) {
		if (!this.isIdRegistered(idProducto)) {
			return false;
		}
		Map<Integer, Precio> savedPreciosMap = this.mapPrecioToTipoCliente(this.precioRepository.findByProducto((long) idProducto));

		this.updatePrecios(savedPreciosMap, newPrecios, idProducto);
		this.precioRepository.saveAll(savedPreciosMap.values().stream().toList());
		return true;
	}

	public void updateStockFromVenta(Venta venta) {
		List<Producto> updated = new LinkedList<>();
		venta.getProductos().forEach(productoVenta ->
			this.updateProductoQuantity(productoVenta).ifPresent(updated::add));
		this.productos.saveAll(updated);
	}

	public Producto newProducto(PublicProducto newProducto) {
		this.productosCache = null;
		return this.productos.save(new Producto(newProducto));
	}

	public Map<Long, Integer> getStockByProductos(List<Long> idProductos) {
		return this.productos.findAll(this.productoInIdsSpecification(idProductos))
			.stream()
			.collect(Collectors.toMap(Producto::getId, Producto::getCantidad));
	}

	public boolean isIdRegistered(int idProducto) {
		return this.productos.countById(idProducto) > 0;
	}

	public boolean isReferenced(int idProducto) {
		return this.productoVenta.countByProducto((long) idProducto) > 0;
	}

	@Transactional
	public boolean deleteById(int idProducto) {
		if (!this.isReferenced(idProducto)) {
			this.productosCache = null;
			this.productos.deleteById(idProducto);
			return true;
		}
		return false;
		// TODO Check if producto is used on another table
	}

	private Optional<Producto> updateProductoQuantity(ProductoVenta productoVenta) {
		return this.getProductoById(Math.toIntExact(productoVenta.getProducto()))
			.map(producto -> {
				producto.setCantidad(producto.getCantidad() - productoVenta.getCantidad());
				return producto;
			});
	}

	private Precio publicToPrecio(PublicPrecio publicPrecio, Long productoId) {
		Precio precio = new Precio();
		precio.setProducto(productoId);
		precio.setCliente((long) publicPrecio.tipoCliente);
		precio.setPrecio(publicPrecio.precio);
		return precio;
	}

	private Optional<Producto> getProductoById(int idProducto) {
		return this.getAll()
			.stream()
			.filter(producto -> producto.getId() == idProducto)
			.findFirst();
	}

	private List<Long> getProductosId(List<Producto> productos) {
		return productos
			.stream()
			.map(Producto::getId)
			.toList();
	}

	private Map<Integer, Precio> mapPrecioToTipoCliente(List<Precio> precios) {
		return precios
			.stream()
			.collect(Collectors.toMap(precio -> Math.toIntExact(precio.getCliente()), precio -> precio));
	}

	private void updatePrecios(Map<Integer, Precio> preciosMap, List<PublicPrecio> newPrecios, int idProducto) {
		newPrecios.forEach(newPrecio -> {
			if (preciosMap.containsKey(newPrecio.tipoCliente)) {
				preciosMap.get(newPrecio.tipoCliente).setPrecio(newPrecio.precio);
			} else {
				preciosMap.put(newPrecio.tipoCliente, new Precio(idProducto, newPrecio.tipoCliente, newPrecio.precio));
			}
		});
	}

	private Specification<Producto> productoInIdsSpecification(List<Long> productosId) {
		return (root, query, builder) -> builder.in(root.get("id")).value(productosId);
	}


}
