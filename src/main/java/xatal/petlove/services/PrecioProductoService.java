package xatal.petlove.services;

import org.antlr.v4.runtime.misc.Pair;
import org.springframework.stereotype.Service;
import xatal.petlove.entities.Precio;
import xatal.petlove.entities.Producto;
import xatal.petlove.mappers.PrecioMapper;
import xatal.petlove.mappers.TipoClienteMapper;
import xatal.petlove.repositories.PrecioRepository;
import xatal.petlove.repositories.ProductoRepository;
import xatal.petlove.structures.MultiDetailedPrecioProducto;
import xatal.petlove.structures.MultiPrecioProducto;
import xatal.petlove.structures.PublicPrecio;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PrecioProductoService {
	private final ProductoRepository productoRepository;
	private final PrecioRepository precioRepository;
	private final SearchProductoService searchProductoService;
	private final ProductoService productoService;
	private final PrecioMapper precioMapper;

	public PrecioProductoService(ProductoRepository productoRepository, PrecioRepository precioRepository,
	                             SearchProductoService searchProductoService, ProductoService productoService, PrecioMapper precioMapper) {
		this.productoRepository = productoRepository;
		this.precioRepository = precioRepository;
		this.searchProductoService = searchProductoService;
		this.productoService = productoService;
		this.precioMapper = precioMapper;
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
		if (!this.productoService.isIdRegistered(idProducto)) {
			return false;
		}
		Map<Integer, Precio> savedPreciosMap = TipoClienteMapper.mapTipoClientePrecio(
			this.precioRepository.findByProducto((long) idProducto));

		this.updatePrecios(savedPreciosMap, newPrecios, idProducto);
		this.precioRepository.saveAll(savedPreciosMap.values().stream().toList());
		return true;
	}
}
