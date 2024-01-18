package xatal.petlove.controllers;

import io.jsonwebtoken.Claims;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import xatal.petlove.entities.Abono;
import xatal.petlove.entities.Producto;
import xatal.petlove.entities.Usuario;
import xatal.petlove.entities.Venta;
import xatal.petlove.reports.PDFVentaReports;
import xatal.petlove.reports.VentasReports;
import xatal.petlove.security.TokenUtils;
import xatal.petlove.services.ProductoService;
import xatal.petlove.services.UsuarioService;
import xatal.petlove.services.VentaService;
import xatal.petlove.structures.FullVenta;
import xatal.petlove.structures.NewAbono;
import xatal.petlove.structures.NewVenta;
import xatal.petlove.structures.PublicAbono;
import xatal.petlove.structures.PublicProductoVenta;
import xatal.petlove.structures.PublicVenta;

import java.util.List;
import java.util.Optional;

@CrossOrigin
@RestController
@RequestMapping("/venta")
public class VentaController {
	private final VentaService ventaService;
	private final UsuarioService usuarioService;
	private final VentasReports reportService;
	private final ProductoService productoService;

	public VentaController(VentaService ventaService, UsuarioService usuarioService, VentasReports reportService, ProductoService productoService) {
		this.ventaService = ventaService;
		this.usuarioService = usuarioService;
		this.reportService = reportService;
		this.productoService = productoService;
	}

	@GetMapping
	public ResponseEntity getVentas() {
		List<Venta> ventas = this.ventaService.getAll();
		if (ventas.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(this.ventaService.publicFromVentas(ventas));
	}

	@GetMapping("/buscar")
	public ResponseEntity searchVentas(
		@RequestHeader("Token") String token,
		@RequestParam(name = "cliente", required = false) Optional<String> clienteNombre,
		@RequestParam(name = "producto", required = false) Optional<Integer> producto,
		@RequestParam(name = "anio", required = false) Optional<Integer> anio,
		@RequestParam(name = "mes", required = false) Optional<Integer> mes,
		@RequestParam(name = "dia", required = false) Optional<Integer> dia,
		@RequestParam(name = "size", required = false, defaultValue = "10") Integer sizePag,
		@RequestParam(name = "pag", required = false, defaultValue = "0") Integer pag,
		@RequestParam(name = "pagado", required = false) Optional<Boolean> pagado,
		@RequestParam(name = "enviar", required = false) Optional<Boolean> enviar
	) {
		List<Venta> ventas = this.ventaService.searchVentas(
			clienteNombre.orElse(null),
			producto.orElse(null),
			anio.orElse(null),
			mes.orElse(null),
			dia.orElse(null),
			pagado.orElse(null),
			sizePag, pag);
		if (ventas.isEmpty()) {
			return ResponseEntity.noContent().build();
		}
		if (enviar.isPresent() && enviar.get()) {
			Claims claims = TokenUtils.getTokenClaims(token);
//			this.reportService.generateReportsFrom(ventas, claims.get("username").toString(), claims.getSubject());
			PDFVentaReports reports = new PDFVentaReports(this.productoService);
			reports.generateReportsFrom(ventas);
			return new ResponseEntity(HttpStatus.CREATED);
		} else {
			return ResponseEntity.ok(ventas);
		}
	}

	@PostMapping
	public ResponseEntity newVenta(
		@RequestHeader("Token") String token,
		@RequestBody NewVenta venta
	) {
		List<PublicProductoVenta> notInStock = this.ventaService.notInStock(venta);
		if (!notInStock.isEmpty()) {
			return new ResponseEntity(notInStock, HttpStatus.CONFLICT);
		}
		Claims claims = TokenUtils.getTokenClaims(token);
		Optional<Usuario> optionalUsuario = this.usuarioService.getUsuarioFromEmail(claims.getSubject());
		if (optionalUsuario.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		venta.vendedor = optionalUsuario.get().getId();
		return this.ventaService.saveNewVenta(venta)
			.map(value -> new ResponseEntity(new PublicVenta(value), HttpStatus.CREATED))
			.orElseGet(() -> new ResponseEntity(HttpStatus.CONFLICT));
	}

	@GetMapping("/{id_venta}")
	public ResponseEntity getVenta(@PathVariable("id_venta") Long ventaId) {
		Optional<Venta> optionalVenta = this.ventaService.getById(ventaId);
		return optionalVenta
			.map(ResponseEntity::ok)
			.orElseGet(() -> ResponseEntity.notFound().build());
	}

	@GetMapping("/{id_venta}/details")
	public ResponseEntity getVentaDetails(@PathVariable("id_venta") Long idVenta) {
		Optional<FullVenta> optionalVenta = this.ventaService.getFullVenta(idVenta);
		if (optionalVenta.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(optionalVenta.get());
	}

	@PutMapping("/{id_venta}")
	public ResponseEntity updateVenta(
		@PathVariable("id_venta") Long ventaId,
		@RequestBody PublicVenta venta
	) {
		venta.id = ventaId;
		Venta updatedVenta = this.ventaService.updateVenta(venta);
		if (updatedVenta == null) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(updatedVenta);
	}

	@DeleteMapping("/{id_venta}")
	public ResponseEntity deleteVenta(@PathVariable("id_venta") Long ventaId) {
		if (!this.ventaService.isIdRegistered(ventaId)) {
			return ResponseEntity.notFound().build();
		}
		this.ventaService.deleteById(ventaId);
		return ResponseEntity.ok().build();
	}

	@GetMapping("/{idVenta}/abono")
	public ResponseEntity getAbonos(@PathVariable("idVenta") Long idVenta) {
		if (!this.ventaService.isIdRegistered(idVenta)) {
			return ResponseEntity.notFound().build();
		}
		List<Abono> abonos = ventaService.getAbonosFromVentaId(idVenta);
		if (abonos.isEmpty()) {
			return ResponseEntity.noContent().build();
		}
		return ResponseEntity.ok(abonos);
	}

	@PostMapping("/{idVenta}/abono")
	public ResponseEntity addAbono(
		@PathVariable("idVenta") Long idVenta,
		@RequestBody NewAbono abono
	) {
		if (!this.ventaService.isIdRegistered(idVenta)) {
			return ResponseEntity.notFound().build();
		}
		abono.venta = idVenta;
		Optional<Abono> savedAbono = this.ventaService.saveNewAbono(abono);
		return savedAbono
			.map(value -> new ResponseEntity(value, HttpStatus.CREATED))
			.orElseGet(() -> ResponseEntity.internalServerError().build());
	}

	@PutMapping("/{idVenta}/abono/{idAbono}")
	public ResponseEntity updateAbono(
		@PathVariable("idVenta") Long idVenta,
		@PathVariable("idAbono") Long idAbono,
		@RequestBody PublicAbono abono
	) {
		if (!this.ventaService.isIdRegistered(idVenta) || !this.ventaService.isAbonoRegistered(idAbono)) {
			return ResponseEntity.notFound().build();
		}
		Abono savedAbono = new Abono(abono);
		savedAbono.setId(idAbono);
		this.ventaService.updateAbono(abono, idAbono);
		return ResponseEntity.ok().build();
	}

	@GetMapping("/{idVenta}/productos")
	public ResponseEntity getProductos(@PathVariable("idVenta") Long idVenta) {
		Optional<Venta> optionalVenta = this.ventaService.getById(idVenta);
		if (optionalVenta.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		List<Producto> productos = this.ventaService.getProductosByVentaReplaceCantidad(optionalVenta.get());
		if (productos.isEmpty()) {
			return ResponseEntity.noContent().build();
		}
		return ResponseEntity.ok(productos);
	}
}
