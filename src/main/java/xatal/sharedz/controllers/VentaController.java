package xatal.sharedz.controllers;

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
import xatal.sharedz.entities.Abono;
import xatal.sharedz.entities.Venta;
import xatal.sharedz.reports.VentasReports;
import xatal.sharedz.security.TokenUtils;
import xatal.sharedz.services.VentaService;
import xatal.sharedz.structures.NewVenta;
import xatal.sharedz.structures.PublicAbono;
import xatal.sharedz.structures.PublicVenta;

import java.util.List;
import java.util.Optional;

@CrossOrigin
@RestController
@RequestMapping("/venta")
public class VentaController {
	private final VentaService ventaService;
	private final VentasReports reportService;

	public VentaController(VentaService ventaService, VentasReports reportService) {
		this.ventaService = ventaService;
		this.reportService = reportService;
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
		@RequestParam(name = "producto", required = false) Optional<String> producto,
		@RequestParam(name = "anio", required = false) Optional<Integer> anio,
		@RequestParam(name = "mes", required = false) Optional<Integer> mes,
		@RequestParam(name = "dia", required = false) Optional<Integer> dia,
		@RequestParam(name = "size", required = false, defaultValue = "10") Integer sizePag,
		@RequestParam(name = "pagado", required = false) Optional<Boolean> pagado,
		@RequestParam(name = "enviar", required = false) Optional<Boolean> enviar
	) {
		List<Venta> ventas = this.ventaService.searchVentas(
			clienteNombre.orElse(null),
			anio.orElse(null),
			mes.orElse(null),
			dia.orElse(null),
			pagado.orElse(null),
			sizePag);
		if (ventas.isEmpty()) {
			return ResponseEntity.noContent().build();
		}
		if (enviar.isPresent() && enviar.get()) {
			Claims claims = TokenUtils.getTokenClaims(token);
			this.reportService.generateReportsFrom(ventas, claims.get("username").toString(), claims.getSubject());
			return new ResponseEntity(HttpStatus.CREATED);
		} else {
			return ResponseEntity.ok(this.ventaService.publicFromVentas(ventas));
		}
	}

	@PostMapping
	public ResponseEntity newVenta(@RequestBody NewVenta venta) {
		return this.ventaService.saveNewVenta(venta)
			.map(value -> new ResponseEntity(new PublicVenta(value), HttpStatus.CREATED))
			.orElseGet(() -> new ResponseEntity(HttpStatus.CONFLICT));
	}

	@GetMapping("/{id_venta}")
	public ResponseEntity getVenta(@PathVariable("id_venta") int ventaId) {
		Optional<Venta> optionalVenta = this.ventaService.getById(ventaId);
		return optionalVenta
			.map(ResponseEntity::ok)
			.orElseGet(() -> ResponseEntity.notFound().build());
	}

	@PutMapping("/{id_venta}")
	public ResponseEntity updateVenta(
		@PathVariable("id_venta") int ventaId,
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
	public ResponseEntity deleteVenta(@PathVariable("id_venta") int ventaId) {
		if (!this.ventaService.isIdRegistered(ventaId)) {
			return ResponseEntity.notFound().build();
		}
		this.ventaService.deleteById(ventaId);
		return ResponseEntity.ok().build();
	}

	@GetMapping("/{idVenta}/abono")
	public ResponseEntity getAbonos(@PathVariable("idVenta") int idVenta) {
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
		@PathVariable("idVenta") int idVenta,
		@RequestBody PublicAbono abono
	) {
		if (!this.ventaService.isIdRegistered(idVenta)) {
			return ResponseEntity.notFound().build();
		}
		abono.venta = idVenta;
		return new ResponseEntity(this.ventaService.saveNewAbono(abono), HttpStatus.CREATED);
	}

	@PutMapping("/{idVenta}/abono/{idAbono}")
	public ResponseEntity updateAbono(
		@PathVariable("idVenta") int idVenta,
		@PathVariable("idAbono") int idAbono,
		@RequestBody PublicAbono abono
	) {
		if (!this.ventaService.isIdRegistered(idVenta) || !this.ventaService.isAbonoRegistered(idAbono)) {
			return ResponseEntity.notFound().build();
		}
		Abono savedAbono = new Abono(abono);
		savedAbono.setId((long) idAbono);
		return ResponseEntity.ok(this.ventaService.saveAbono(savedAbono));
	}
}
