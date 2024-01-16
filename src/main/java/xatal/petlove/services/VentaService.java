package xatal.petlove.services;

import jakarta.transaction.Transactional;
import org.antlr.v4.runtime.misc.Pair;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import xatal.petlove.entities.Abono;
import xatal.petlove.entities.Producto;
import xatal.petlove.entities.ProductoVenta;
import xatal.petlove.entities.Venta;
import xatal.petlove.repositories.AbonoRepository;
import xatal.petlove.repositories.ProductoVentaRepository;
import xatal.petlove.repositories.VentaRepository;
import xatal.petlove.structures.NewAbono;
import xatal.petlove.structures.NewVenta;
import xatal.petlove.structures.PublicAbono;
import xatal.petlove.structures.PublicProductoVenta;
import xatal.petlove.structures.PublicVenta;
import xatal.petlove.util.Util;

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

	public VentaService(
		VentaRepository ventaRepository,
		ProductoVentaRepository productoVentaRepository, AbonoRepository abonoRepository,
		ClienteService clienteService, ProductoService productoService
	) {
		this.ventaRepository = ventaRepository;
		this.productoVentaRepository = productoVentaRepository;
		this.abonoRepository = abonoRepository;
		this.clienteService = clienteService;
		this.productoService = productoService;
	}

	public List<Venta> getAll() {
		return this.ventaRepository.getAll();
	}

	public List<Venta> searchVentas(
		String nombreCliente,
		Integer producto,
		Integer year,
		Integer month,
		Integer day,
		Boolean pagado,
		Integer size,
		Integer pag
	) {
		Specification<Venta> spec = Specification.where(null);
		spec = this.addNombreClienteSpecification(nombreCliente, spec);
		spec = this.addProductoSpecification(producto, spec);
		spec = this.addYearSpecification(year, spec);
		spec = this.addMonthSpecification(month, spec);
		spec = this.addDaySpecification(day, spec);
		spec = this.addPagadoSpecification(pagado, spec);
		spec = this.orderByNewer(spec);
		Pageable pageable = PageRequest.of(pag, size);
		return this.ventaRepository.findAll(spec, pageable).stream().toList();
	}

	public Optional<Venta> saveNewVenta(NewVenta newVenta, Long idVendedor) {
		Pair<Venta, Float> pair = this.buildFromNewVenta(newVenta);
		List<Long> productosId = newVenta.productos
			.stream()
			.map(productoVenta -> productoVenta.producto)
			.toList();

		if (!this.productsOnStock(newVenta.productos, this.productoService.getStockByProductos(productosId))) {
			return Optional.empty();
		}

		Venta ventaToSave = pair.a;
		ventaToSave.setVendedor(idVendedor);
		ventaToSave.setAbonado(pair.b);

		ventaToSave.setTotal(this.getCostoTotalByVenta(ventaToSave));
		ventaToSave.setPagado(ventaToSave.getAbonado() >= ventaToSave.getTotal());

		Venta savedVenta = this.saveVentaWithProductos(ventaToSave);
		this.abonoRepository.save(new Abono(savedVenta.getId().intValue(), ventaToSave.getAbonado(), new Date()));
		this.updateStocks(savedVenta);
		return Optional.of(savedVenta);
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
			this.setPagadoOnVenta(venta);
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

	public Venta buildFromPublicVenta(PublicVenta publicVenta) {
		Venta aux = new Venta();
		aux.setCliente(this.clienteService.getById(publicVenta.cliente));
		aux.setPagado(publicVenta.pagado);
		aux.setFecha(Util.dateFromString(publicVenta.fecha));
		aux.setProductos(publicVenta.productos.stream().map(ProductoVenta::new).toList());
		aux.setAbonado(publicVenta.abonado);
		aux.setTotal(publicVenta.total);
		return aux;
	}

	public Pair<Venta, Float> buildFromNewVenta(NewVenta newVenta) {
		return new Pair<>(this.buildFromPublicVenta(newVenta), newVenta.abono);
	}

	public List<PublicVenta> publicFromVentas(List<Venta> ventas) {
		return ventas
			.stream()
			.map(PublicVenta::new)
			.toList();
	}

	public List<PublicProductoVenta> notInStock(NewVenta newVenta) {
		List<PublicProductoVenta> unavailable = new LinkedList<>();
		List<Long> idProductos = newVenta.productos.stream().map(productoVenta -> productoVenta.producto).toList();
		Map<Long, Integer> stock = this.productoService.getStockByProductos(idProductos);

		newVenta.productos.forEach(productoVenta -> {
			if (!stock.containsKey(productoVenta.producto)) {
				unavailable.add(new PublicProductoVenta(productoVenta.producto, productoVenta.cantidad));
			} else if (stock.get(productoVenta.producto) < productoVenta.cantidad) {
				unavailable.add(new PublicProductoVenta(productoVenta.producto, stock.get(productoVenta.producto)));
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
		this.abonoRepository.deleteAbonosByVenta(idVenta);
		this.productoVentaRepository.deleteAll(venta.getProductos());
		this.ventaRepository.deleteById(idVenta);
	}

	private Specification<Venta> addNombreClienteSpecification(String nombreCliente, Specification<Venta> spec) {
		if (nombreCliente != null && !nombreCliente.isEmpty()) {
			spec = spec.and((root, query, builder) ->
				builder.like(builder.lower(root.get("cliente").get("nombre")),
					"%" + nombreCliente.toLowerCase() + "%"));
		}
		return spec;
	}

	private Specification<Venta> addProductoSpecification(Integer producto, Specification<Venta> spec) {
		if (producto != null) {
			spec = spec.and((root, query, builder) -> builder.equal(root.get("productos").get("producto"), producto));
		}
		return spec;
	}

	private Specification<Venta> addDaySpecification(Integer day, Specification<Venta> spec) {
		if (day != null) {
			spec = spec.and((root, query, builder) ->
				builder.equal(builder.function("day", Integer.class, root.get("fecha")), day));
		}
		return spec;
	}

	private Specification<Venta> addMonthSpecification(Integer month, Specification<Venta> spec) {
		if (month != null) {
			spec = spec.and((root, query, builder) ->
				builder.equal(builder.function("month", Integer.class, root.get("fecha")), month));
		}
		return spec;
	}

	private Specification<Venta> addYearSpecification(Integer year, Specification<Venta> spec) {
		if (year != null) {
			spec = spec.and((root, query, builder) ->
				builder.equal(builder.function("year", Integer.class, root.get("fecha")), year));
		}
		return spec;
	}

	private Specification<Venta> addPagadoSpecification(Boolean pagado, Specification<Venta> spec) {
		if (pagado != null) {
			spec = spec.and((root, query, builder) -> builder.equal(root.get("pagado"), pagado));
		}
		return spec;
	}

	private Specification<Venta> orderByNewer(Specification<Venta> spec) {
		return spec
			.and((root, query, builder) -> query.orderBy(builder.desc(root.get("fecha"))).getRestriction());
	}

	private Specification<Abono> addSumAbonosByVentasSpecification(List<Venta> ventas, Specification<Abono> spec) {
		if (ventas != null && !ventas.isEmpty()) {
			List<Long> idVentas = ventas.stream().map(Venta::getId).toList();
			spec = spec.and((root, query, builder) -> {
				query.multiselect(root.get("venta"), builder.sum(root.get("cantidad")))
					.where(builder.in(root.get("venta")).value(idVentas))
					.groupBy(root.get("venta"));
				return query.getRestriction();
			});
		}
		return spec;
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

	private float getCostoTotalByVenta(Venta venta) {
		final float CERO = 0;
		Map<Long, Float> productos = this.getProductosByVenta(venta)
			.stream()
			.collect(Collectors.toMap(Producto::getId, Producto::getPrecio));

		return venta.getProductos()
			.stream()
			.map(productoVenta -> productos.get(productoVenta.getProducto()) * productoVenta.getCantidad())
			.reduce(CERO, Float::sum);
	}

	private void updateStocks(Venta venta) {
		this.productoService.updateStockFromVenta(venta);
	}

	private void setPagadoOnVenta(Venta venta) {
		float total = this.getCostoTotalByVenta(venta);
		float abonos = this.getTotalAbonosByVentaId(Math.toIntExact(venta.getId()));
		venta.setAbonado(abonos);
		venta.setTotal(total);
		venta.setPagado(abonos >= total);
	}

	private boolean productsOnStock(List<PublicProductoVenta> productos, Map<Long, Integer> stock) {
		return productos
			.stream()
			.allMatch(productoVenta ->
				stock.containsKey(productoVenta.producto)
					&& stock.get(productoVenta.producto) >= productoVenta.cantidad
			);
	}
}