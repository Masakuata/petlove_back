package xatal.petlove.services;

import org.springframework.stereotype.Service;
import xatal.petlove.entities.Precio;
import xatal.petlove.entities.Producto;
import xatal.petlove.mappers.PrecioMapper;
import xatal.petlove.mappers.ProductoMapper;
import xatal.petlove.mappers.TipoClienteMapper;
import xatal.petlove.repositories.PrecioRepository;
import xatal.petlove.repositories.ProductoRepository;
import xatal.petlove.structures.DetailedPrecio;
import xatal.petlove.structures.MultiDetailedPrecioProducto;
import xatal.petlove.structures.MultiPrecioProducto;
import xatal.petlove.structures.PublicPrecio;

import java.util.List;
import java.util.Map;

@Service
public class PrecioProductoService {
	private final ProductoRepository productoRepository;
	private final PrecioRepository precioRepository;
	private final PrecioMapper precioMapper;

	public PrecioProductoService(ProductoRepository productoRepository, PrecioRepository precioRepository,
	                             PrecioMapper precioMapper) {
		this.productoRepository = productoRepository;
		this.precioRepository = precioRepository;
		this.precioMapper = precioMapper;
	}

	public List<MultiPrecioProducto> getWithPrecios() {
		Map<Long, MultiPrecioProducto> multiPrecioMap =
			ProductoMapper.mapIdMultiPrecioProducto(this.productoRepository.getAll());

		this.precioRepository.getAll().forEach(precio -> {
			if (multiPrecioMap.containsKey(precio.getProducto())) {
				multiPrecioMap.get(precio.getProducto()).precios.add(new PublicPrecio(precio));
			}
		});
		return multiPrecioMap.values().stream().toList();
	}

	public MultiPrecioProducto getWithPrecios(Producto producto) {
		MultiPrecioProducto multiProducto = new MultiPrecioProducto(producto);
		multiProducto.precios = this.precioRepository.findByProducto(producto.getId())
			.stream()
			.map(PublicPrecio::new)
			.toList();
		return multiProducto;
	}

	public MultiDetailedPrecioProducto getWithPreciosAndTipoCliente(Producto producto) {
		MultiDetailedPrecioProducto multiDetailedProducto = new MultiDetailedPrecioProducto(producto);
		multiDetailedProducto.precios = this.precioMapper.publicToDetailed(this.getWithPrecios(producto).precios);
		return multiDetailedProducto;
	}

	public List<DetailedPrecio> getDetailedPrecios(long idProducto) {
		return this.precioMapper.precioToDetailed(this.precioRepository.findByProducto(idProducto));
	}

	public void setProductosPrices(List<Producto> productos, long tipoCliente) {
		List<Long> idProductos = productos.stream().map(Producto::getId).toList();
		Map<Long, Precio> mapProductoPrecio =
			this.precioMapper.mapProductoPrecio(this.precioRepository.findByProductoInAndCliente(idProductos, tipoCliente));

		productos.forEach(producto -> {
			if (mapProductoPrecio.containsKey(producto.getId())) {
				producto.setPrecio(mapProductoPrecio.get(producto.getId()).getPrecio());
			}
		});
	}

	private void updatePrecios(Map<Long, Precio> preciosMap, List<PublicPrecio> newPrecios, int idProducto) {
		newPrecios.forEach(newPrecio -> {
			if (preciosMap.containsKey((long) newPrecio.id)) {
				preciosMap.get((long) newPrecio.id).setPrecio(newPrecio.precio);
			} else {
				preciosMap.put((long) newPrecio.id, new Precio(idProducto, newPrecio.id, newPrecio.precio));
			}
		});
	}

	public boolean savePreciosById(int idProducto, List<PublicPrecio> newPrecios) {
		Map<Long, Precio> savedPreciosMap = TipoClienteMapper.mapTipoClientePrecio(
			this.precioRepository.findByProducto((long) idProducto)
		);
		this.updatePrecios(savedPreciosMap, newPrecios, idProducto);
		this.precioRepository.saveAll(savedPreciosMap.values().stream().toList());
		return true;
	}
}
