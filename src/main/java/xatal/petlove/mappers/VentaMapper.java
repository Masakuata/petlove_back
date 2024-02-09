package xatal.petlove.mappers;

import org.springframework.stereotype.Component;
import xatal.petlove.entities.Direccion;
import xatal.petlove.entities.ProductoVenta;
import xatal.petlove.entities.Venta;
import xatal.petlove.services.ClienteService;
import xatal.petlove.services.SearchProductoService;
import xatal.petlove.services.UsuarioService;
import xatal.petlove.structures.FullVenta;
import xatal.petlove.structures.NewVenta;
import xatal.petlove.structures.PublicCliente;
import xatal.petlove.structures.PublicProductoVenta;
import xatal.petlove.structures.PublicVenta;
import xatal.petlove.util.Util;

import java.util.List;

@Component
public class VentaMapper {
	private final ClienteService clienteService;
	private final SearchProductoService searchProductoService;
	private final UsuarioService usuarioService;

	public VentaMapper(ClienteService clienteService, SearchProductoService searchProductoService,
	                   UsuarioService usuarioService) {
		this.clienteService = clienteService;
		this.searchProductoService = searchProductoService;
		this.usuarioService = usuarioService;
	}

	public Venta newVentaToVenta(NewVenta newVenta) {
		Venta aux = new Venta();
		aux.setCliente(this.clienteService.getById(newVenta.cliente));
		aux.setPagado(newVenta.pagado);
		aux.setFecha(Util.dateFromString(newVenta.fecha));
		aux.setProductos(newVenta.productos.stream().map(ProductoVenta::new).toList());
		aux.setAbonado(newVenta.abono);
		aux.setTotal(newVenta.total);
		aux.setVendedor(newVenta.vendedor);
		aux.setDireccion(newVenta.direccion);
		return aux;
	}

	public Venta publicVentaToVenta(PublicVenta publicVenta) {
		Venta aux = new Venta();
		aux.setCliente(this.clienteService.getById(publicVenta.cliente));
		aux.setPagado(publicVenta.pagado);
		aux.setFecha(Util.dateFromString(publicVenta.fecha));
		aux.setProductos(publicVenta.productos.stream().map(ProductoVenta::new).toList());
		aux.setAbonado(publicVenta.abonado);
		aux.setTotal(publicVenta.total);
		aux.setVendedor(publicVenta.vendedor);
		aux.setDireccion(
			aux.getCliente().getDireccionByString(publicVenta.direccion)
				.map(Direccion::getId)
				.orElse(0L)
		);
		return aux;
	}

	public PublicVenta ventaToPublicVenta(Venta venta) {
		PublicVenta aux = new PublicVenta();
		aux.id = venta.getId();
		aux.cliente = venta.getCliente().getId();
		aux.vendedor = venta.getVendedor();
		aux.pagado = venta.isPagado();
		aux.fecha = Util.dateToString(venta.getFecha());
		aux.facturado = venta.isFacturado();
		aux.abonado = venta.getAbonado();
		aux.total = venta.getTotal();
		aux.direccion = venta.getCliente().getDireccionById(venta.getDireccion())
			.map(Direccion::getDireccion)
			.orElse("");
		aux.productos = venta.getProductos()
			.stream()
			.map(PublicProductoVenta::new)
			.toList();
		return aux;
	}

	public FullVenta ventaToFullVenta(Venta venta) {
		FullVenta aux = new FullVenta();
		aux.cliente = new PublicCliente(venta.getCliente());
		aux.vendedor = this.usuarioService.getById(venta.getVendedor()).getUsername();
		aux.pagado = venta.isPagado();
		aux.fecha = Util.dateToString(venta.getFecha());
		aux.facturado = venta.isFacturado();
		aux.abonado = venta.getAbonado();
		aux.total = venta.getTotal();
		aux.direccion = venta.getCliente().getDireccionById(venta.getDireccion())
			.map(Direccion::getDireccion)
			.orElse("");
		List<Integer> idProductos = venta.getProductos()
			.stream()
			.map(productoVenta -> productoVenta.getId().intValue())
			.toList();

		aux.productos = this.searchProductoService.searchByIdsAndTipoCliente(idProductos, venta.getCliente().getTipoCliente());
		return aux;
	}

	public List<PublicVenta> ventasToPublic(List<Venta> ventas) {
		return ventas
			.stream()
			.map(this::ventaToPublicVenta)
			.toList();
	}

	public List<Integer> getIdProductos(Venta venta) {
		return venta
			.getProductos()
			.stream()
			.map(productoVenta -> Math.toIntExact(productoVenta.getProducto()))
			.toList();
	}
}
