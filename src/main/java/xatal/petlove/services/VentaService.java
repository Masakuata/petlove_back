package xatal.petlove.services;

import jakarta.transaction.Transactional;
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
	private final AbonoRepository abonoRepository;
	private final ProductoVentaRepository productoVentaRepository;
	private final ClienteService clienteService;
	private final ProductoService productoService;
	private final SearchProductoService searchProductoService;
	private final SearchVentaService searchVentaService;
	private final UsuarioService usuarioService;
	private final PDFVentaReports ventaReports;
	private final VentaMapper ventaMapper;

	public VentaService(
		VentaRepository ventaRepository, AbonoRepository abonoRepository, ProductoVentaRepository productoVentaRepository,
		ClienteService clienteService, ProductoService productoService, SearchProductoService searchProductoService,
		SearchVentaService searchVentaService, UsuarioService usuarioService, VentaMapper ventaMapper
	) {
		this.ventaRepository = ventaRepository;
		this.abonoRepository = abonoRepository;
		this.productoVentaRepository = productoVentaRepository;
		this.clienteService = clienteService;
		this.productoService = productoService;
		this.searchProductoService = searchProductoService;
		this.searchVentaService = searchVentaService;
		this.usuarioService = usuarioService;
		this.ventaMapper = ventaMapper;
		this.ventaReports = new PDFVentaReports(this.productoService, this.searchProductoService);
	}

	public Venta saveNewVenta(NewVenta newVenta) {
		Venta venta = this.ventaMapper.newVentaToVenta(newVenta);
		venta.setPagado(venta.getAbonado() >= venta.getTotal());
		venta.setDireccion(newVenta.direccion);
		venta = this.saveVentaWithProductos(venta);

		this.storeAbono(new Abono(venta.getId().intValue(), venta.getAbonado(), new Date()));
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

	public Venta storeVenta(Venta venta) {
		return this.ventaRepository.save(venta);
	}

	public Venta updateVenta(PublicVenta publicVenta) {
		return this.getById(publicVenta.id)
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

	@Transactional
	public void deleteById(Long idVenta) {
		Optional<Venta> optionalVenta = this.getById(idVenta);
		if (optionalVenta.isEmpty()) {
			return;
		}
		Venta venta = optionalVenta.get();
		venta.getProductos().forEach(productoVenta ->
			this.productoService.returnStock(productoVenta.getProducto(), productoVenta.getCantidad()));
		this.deleteAbonosByVentaId(idVenta);
		this.productoVentaRepository.deleteAll(venta.getProductos());
		this.ventaRepository.deleteById(idVenta);
	}

	private List<Producto> getProductosByVenta(Venta venta) {
		List<Integer> productosId = venta.getProductos()
			.stream()
			.map(productoVenta -> productoVenta.getProducto().intValue())
			.toList();
		return this.searchProductoService
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

	public void setVentaPagado(Venta venta) {
		float total = this.getCostoTotalByVenta(venta);
		float abonos = this.getTotalAbonosByVentaId(Math.toIntExact(venta.getId()));
		venta.setAbonado(abonos);
		venta.setTotal(total);
		venta.setPagado(abonos >= total);
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

	public Abono storeAbono(Abono abono) {
		return this.abonoRepository.save(abono);
	}

	public Optional<Abono> saveNewAbono(NewAbono newAbono) {
		Optional<Venta> optionalVenta = this.searchVentaService.getById(newAbono.venta);
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
		Abono savedAbono = this.abonoRepository.save(new Abono(newAbono));
		this.storeVenta(venta);
		return Optional.of(savedAbono);
	}

	public Abono updateAbono(PublicAbono publicAbono, long idAbono) {
		this.abonoRepository.updateAbonoCantidad(idAbono, publicAbono.cantidad);
		Optional<Venta> optionalVenta = this.searchVentaService.getById(publicAbono.venta);
		optionalVenta.ifPresent(venta -> {
			venta.setAbonado(this.abonoRepository.sumAbonosByVenta(venta.getId()));
			this.storeVenta(venta);
		});
		Abono abono = new Abono(publicAbono);
		abono.setId(idAbono);
		return abono;
	}

	public List<Abono> getAbonosFromVentaId(long idVenta) {
		return this.abonoRepository.findByVenta(idVenta);
	}

	public boolean isAbonoRegistered(long idAbono) {
		return this.abonoRepository.countById(idAbono) > 0;
	}

	public float getTotalAbonosByVentaId(long idVenta) {
		return this.abonoRepository.sumAbonosByVenta(idVenta);
	}

	public void deleteAbonosByVentaId(long idVenta) {
		this.abonoRepository.deleteAbonosByVenta(idVenta);
	}

	public List<Integer> getAniosVentas() {
		return this.ventaRepository.getDistinctYear();
	}
}
