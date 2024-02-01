package xatal.petlove.services;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import xatal.petlove.entities.Abono;
import xatal.petlove.entities.Producto;
import xatal.petlove.entities.ProductoVenta;
import xatal.petlove.entities.Venta;
import xatal.petlove.mappers.VentaMapper;
import xatal.petlove.reports.PDFVentaReports;
import xatal.petlove.repositories.AbonoRepository;
import xatal.petlove.repositories.ProductoVentaRepository;
import xatal.petlove.repositories.VentaRepository;
import xatal.petlove.services.specifications.VentaSpecification;
import xatal.petlove.structures.FullVenta;
import xatal.petlove.structures.NewAbono;
import xatal.petlove.structures.NewVenta;
import xatal.petlove.structures.PublicAbono;
import xatal.petlove.structures.PublicProductoVenta;
import xatal.petlove.structures.PublicVenta;
import xatal.petlove.util.Util;

import java.io.IOException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class VentaService {
	private final VentaRepository ventaRepository;
	private final ProductoVentaRepository productoVentaRepository;
	private final AbonoRepository abonoRepository;
	private final ClienteService clienteService;
	private final ProductoService productoService;
	private final UsuarioService usuarioService;
	private final PDFVentaReports ventaReports;
	private final VentaMapper ventaMapper;

	public VentaService(
		VentaRepository ventaRepository,
		ProductoVentaRepository productoVentaRepository, AbonoRepository abonoRepository,
		ClienteService clienteService, ProductoService productoService, UsuarioService usuarioService, VentaMapper ventaMapper
	) {
		this.ventaRepository = ventaRepository;
		this.productoVentaRepository = productoVentaRepository;
		this.abonoRepository = abonoRepository;
		this.clienteService = clienteService;
		this.productoService = productoService;
		this.usuarioService = usuarioService;
		this.ventaMapper = ventaMapper;
		this.ventaReports = new PDFVentaReports(this.productoService);
	}

	public List<Venta> getAll() {
		return this.ventaRepository.getAll();
	}

	public List<Venta> searchVentas(
		Integer idCliente,
		String nombreCliente,
		Integer producto,
		Integer year,
		Integer month,
		Integer day,
		Boolean pagado,
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
			VentaSpecification.orderByNewer()
		);
		Pageable pageable = PageRequest.of(pag, size);
		List<Venta> ventas = new java.util.ArrayList<>(this.ventaRepository.findAll(spec, pageable).stream().toList());
		return this.filterByProducto(ventas, producto);
	}

	public Venta saveNewVenta(NewVenta newVenta) {
		Venta venta = this.ventaMapper.newVentaToVenta(newVenta);
		venta.setPagado(venta.getAbonado() >= venta.getTotal());
		venta.setDireccion(newVenta.direccion);
		venta = this.saveVentaWithProductos(venta);

		this.abonoRepository.save(new Abono(venta.getId().intValue(), venta.getAbonado(), new Date()));
		this.productoService.updateStockFromVenta(venta);
		this.generateReport(venta);
		return venta;
	}

	public Venta saveVentaWithProductos(Venta venta) {
		List<ProductoVenta> productosGuardados = new LinkedList<>();
		this.productoVentaRepository.saveAll(venta.getProductos())
			.forEach(productosGuardados::add);
		venta.setProductos(productosGuardados);
		return this.ventaRepository.save(venta);
	}

	public Optional<Abono> saveNewAbono(NewAbono newAbono) {
		Optional<Venta> optionalVenta = this.ventaRepository.getById(newAbono.venta);
		if (optionalVenta.isEmpty()) {
			return Optional.empty();
		}
		Venta venta = optionalVenta.get();
		if (newAbono.finiquito) {
			newAbono.cantidad = venta.getTotal() - venta.getAbonado();
			venta.setPagado(true);
		} else {
			this.setVentaPagado(venta);
		}
		venta.setAbonado(venta.getAbonado() + newAbono.cantidad);
		if (Util.isFechaDefault(newAbono.fecha)) {
			newAbono.fecha = Util.dateToString(new Date());
		}
		Abono savedAbono = this.abonoRepository.save(new Abono(newAbono));
		this.ventaRepository.save(venta);
		return Optional.of(savedAbono);
	}

	public Abono updateAbono(PublicAbono publicAbono, Long idAbono) {
		this.abonoRepository.updateAbonoCantidad(idAbono, publicAbono.cantidad);
		Optional<Venta> optionalVenta = this.ventaRepository.getById(publicAbono.venta);
		optionalVenta.ifPresent(venta -> {
			venta.setAbonado(this.abonoRepository.sumAbonosByVenta(venta.getId()));
			this.ventaRepository.save(venta);
		});
		Abono abono = new Abono(publicAbono);
		abono.setId(idAbono);
		return abono;
	}

	public List<Abono> getAbonosFromVentaId(Long idVenta) {
		return this.abonoRepository.findByVenta(idVenta);
	}

	public boolean isAbonoRegistered(Long idAbono) {
		return this.abonoRepository.countById(idAbono) > 0;
	}

	public float getTotalAbonosByVentaId(int ventaId) {
		return this.abonoRepository.sumAbonosByVenta((long) ventaId);
	}

	public Venta updateVenta(PublicVenta publicVenta) {
		Optional<Venta> storedVenta = this.getById(publicVenta.id);
		return storedVenta
			.map(venta -> this.updateVenta(publicVenta, venta))
			.orElse(null);
	}

	public Venta updateVenta(PublicVenta publicVenta, Venta storedVenta) {
		if (publicVenta.cliente != -1) {
			storedVenta.setCliente(this.clienteService.getById(publicVenta.cliente));
		}
		if (Util.isNotDefaultFecha(publicVenta.fecha)) {
			storedVenta.setFecha(Util.dateFromString(publicVenta.fecha));
		}
		storedVenta.setPagado(publicVenta.pagado);
		storedVenta.setFacturado(publicVenta.facturado);
		return this.ventaRepository.save(storedVenta);
	}

	public List<PublicProductoVenta> getUnavailableProducts(NewVenta newVenta) {
		List<PublicProductoVenta> unavailable = new LinkedList<>();
		List<Long> idProductos = newVenta.productos.stream().map(productoVenta -> productoVenta.producto).toList();
		Map<Long, Integer> stock = this.productoService.getStockByProductos(idProductos);

		newVenta.productos.forEach(productoVenta -> {
			if (!stock.containsKey(productoVenta.producto)) {
				unavailable.add(
					new PublicProductoVenta(productoVenta.producto, productoVenta.cantidad, productoVenta.precio));
			} else if (stock.get(productoVenta.producto) < productoVenta.cantidad) {
				unavailable.add(
					new PublicProductoVenta(productoVenta.producto, stock.get(productoVenta.producto), productoVenta.precio));
			}
		});
		return unavailable;
	}

	public Optional<Venta> getById(Long id) {
		return this.ventaRepository.getById(id);
	}

	public boolean isIdRegistered(Long id) {
		return this.ventaRepository.countById(id) > 0;
	}

	public List<ProductoVenta> getProductoVentaByProducto(Long idProducto) {
		return this.productoVentaRepository.findByProducto(idProducto);
	}

	@Transactional
	public void deleteById(Long idVenta) {
		Optional<Venta> optionalVenta = this.getById(idVenta);
		if (optionalVenta.isEmpty()) {
			return;
		}
		Venta venta = optionalVenta.get();
		venta.getProductos().forEach(productoVenta ->
			this.productoService.returnStock(productoVenta.getProducto(), productoVenta.getCantidad()));
		this.abonoRepository.deleteAbonosByVenta(idVenta);
		this.productoVentaRepository.deleteAll(venta.getProductos());
		this.ventaRepository.deleteById(idVenta);
	}

	private List<Venta> filterByProducto(List<Venta> ventas, Integer producto) {
		if (producto == null) {
			return ventas;
		}
		Long idProducto = Long.valueOf(producto);
		Map<Long, ProductoVenta> productosMap = this.mapProductoVenta(this.getProductoVentaByProducto(idProducto));
		return ventas
			.stream()
			.filter(venta ->
				venta.getProductos()
					.stream()
					.anyMatch(productoVenta -> productosMap.containsKey(productoVenta.getId())))
			.toList();
	}

	private List<Producto> getProductosByVenta(Venta venta) {
		List<Integer> productosId = venta.getProductos()
			.stream()
			.map(productoVenta -> productoVenta.getProducto().intValue())
			.toList();
		return this.productoService
			.searchByIdsAndTipoCliente(productosId, venta.getCliente().getTipoCliente());
	}

	public List<Producto> getProductosByVentaReplaceCantidad(Venta venta) {
		List<Producto> productos = this.getProductosByVenta(venta);
		Map<Long, Integer> productQuantities = venta.getProductos()
			.stream()
			.collect(Collectors.toMap(ProductoVenta::getProducto, ProductoVenta::getCantidad));
		productos.forEach(producto -> producto.setCantidad(productQuantities.get(producto.getId())));
		return productos;
	}

	public Optional<FullVenta> getFullVenta(long idVenta) {
		Optional<Venta> optionalVenta = this.getById(idVenta);
		if (optionalVenta.isEmpty()) {
			return Optional.empty();
		}
		Venta venta = optionalVenta.get();
		FullVenta fullVenta = this.ventaMapper.ventaToFullVenta(venta);
		fullVenta.productos = this.getProductosByVentaReplaceCantidad(venta);
		fullVenta.vendedor = this.usuarioService.getById(venta.getVendedor()).getUsername();
		return Optional.of(fullVenta);
	}

	public float getCostoTotalByVenta(Venta venta) {
		final float CERO = 0;
		Map<Long, Float> productos = this.getProductosByVenta(venta)
			.stream()
			.collect(Collectors.toMap(Producto::getId, Producto::getPrecio));

		return venta.getProductos()
			.stream()
			.map(productoVenta -> productos.get(productoVenta.getProducto()) * productoVenta.getCantidad())
			.reduce(CERO, Float::sum);
	}

	private void setVentaPagado(Venta venta) {
		float total = this.getCostoTotalByVenta(venta);
		float abonos = this.getTotalAbonosByVentaId(Math.toIntExact(venta.getId()));
		venta.setAbonado(abonos);
		venta.setTotal(total);
		venta.setPagado(abonos >= total);
	}

	private Map<Long, ProductoVenta> mapProductoVenta(List<ProductoVenta> productoVenta) {
		return productoVenta
			.stream()
			.collect(Collectors.toMap(ProductoVenta::getId, productoVenta1 -> productoVenta1));
	}

	public boolean productsOnStock(List<PublicProductoVenta> productos) {
		List<Long> productosId = productos
			.stream()
			.map(publicProductoVenta -> publicProductoVenta.producto)
			.toList();
		Map<Long, Integer> stock = this.productoService.getStockByProductos(productosId);
		return productos
			.stream()
			.allMatch(productoVenta ->
				stock.containsKey(productoVenta.producto)
					&& stock.get(productoVenta.producto) >= productoVenta.cantidad
			);
	}

	private void generateReport(Venta venta) {
		new Thread(() -> {
			try {
				this.ventaReports.generateReportAndSend(venta);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}).start();
	}
}
