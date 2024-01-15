package xatal.petlove.services;

import jakarta.transaction.Transactional;
import org.antlr.v4.runtime.misc.Pair;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import xatal.petlove.entities.Precio;
import xatal.petlove.entities.Producto;
import xatal.petlove.entities.ProductoVenta;
import xatal.petlove.entities.Venta;
import xatal.petlove.repositories.PrecioRepository;
import xatal.petlove.repositories.ProductoRepository;
import xatal.petlove.repositories.ProductoVentaRepository;
import xatal.petlove.structures.MultiPrecioProducto;
import xatal.petlove.structures.ProductoLoad;
import xatal.petlove.structures.PublicPrecio;
import xatal.petlove.structures.PublicProducto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductoService {
	private final ProductoRepository productoRepository;
	private final ProductoVentaRepository productoVenta;
	private final PrecioRepository precioRepository;

	public ProductoService(ProductoRepository productoRepository, ProductoVentaRepository productoVenta, PrecioRepository precioRepository) {
		this.productoRepository = productoRepository;
		this.productoVenta = productoVenta;
		this.precioRepository = precioRepository;
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

	public List<Producto> getAll() {
		return this.productoRepository.getAll();
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

	public List<Producto> search(
		String nombre,
		Integer tipoCliente,
		Integer size,
		Integer pag
	) {
		Specification<Producto> spec = Specification.where(null);
		spec = this.addSpecNombreInProducto(nombre, spec);
		Pageable pageable = PageRequest.of(pag, size);
		List<Producto> productos = this.productoRepository.findAll(spec, pageable).stream().toList();
		if (tipoCliente != null) {
			this.setProductosPrices(productos, tipoCliente.longValue());
		}
		return productos;
	}

	public List<Producto> searchByIdsAndTipoCliente(List<Integer> ids, int tipoCliente) {
		List<Producto> productos = this.productoRepository.findByIdIn(ids.stream().map(Integer::longValue).toList());
		this.setProductosPrices(productos, tipoCliente);
		return productos;
	}

	public Optional<Producto> getByIdAndTipoCliente(int idProducto, int tipoCliente) {
		Optional<Producto> producto = this.getProductoById(idProducto);
		producto.ifPresent(currentProducto -> this.setProductPrice(currentProducto, tipoCliente));
		return producto;
	}

	public void setProductPrice(Producto producto, int tipoCliente) {
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
		this.productoRepository.saveAll(updated);
	}

	public void returnStock(long idProducto, int cantidad) {
		this.productoRepository.returnStock(idProducto, cantidad);
	}

	public Producto saveProducto(PublicProducto newProducto) {
		return this.productoRepository.save(new Producto(newProducto));
	}

	public Producto saveProducto(Producto producto) {
		return this.productoRepository.save(producto);
	}

	public Map<Long, Integer> getStockByProductos(List<Long> idProductos) {
		return this.productoRepository.findAll(this.productoInIdsSpecification(idProductos))
			.stream()
			.collect(Collectors.toMap(Producto::getId, Producto::getCantidad));
	}

	public boolean isIdRegistered(int idProducto) {
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
		precio.setCliente((long) publicPrecio.id);
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
			if (preciosMap.containsKey(newPrecio.id)) {
				preciosMap.get(newPrecio.id).setPrecio(newPrecio.precio);
			} else {
				preciosMap.put(newPrecio.id, new Precio(idProducto, newPrecio.id, newPrecio.precio));
			}
		});
	}

	private Specification<Producto> productoInIdsSpecification(List<Long> productosId) {
		return (root, query, builder) -> builder.in(root.get("id")).value(productosId);
	}

	private Specification<Producto> addSpecNombreInProducto(String nombre, Specification<Producto> spec) {
		if (nombre != null && !nombre.isEmpty()) {
			spec = spec.and(((root, query, builder) ->
				builder.like(builder.lower(root.get("nombre")),
					"%" + nombre.toLowerCase() + "%")));
		}
		return spec;
	}
}
