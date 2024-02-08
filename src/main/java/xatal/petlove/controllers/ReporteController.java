package xatal.petlove.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import xatal.petlove.entities.Venta;
import xatal.petlove.services.ReporteService;
import xatal.petlove.services.SearchVentaService;

import java.util.List;
import java.util.Optional;

@CrossOrigin
@RestController
@RequestMapping("/reporte")
public class ReporteController {
	private final SearchVentaService searchVentaService;
	private final ReporteService reporteService;

	public ReporteController(SearchVentaService searchVentaService, ReporteService reporteService) {
		this.searchVentaService = searchVentaService;
		this.reporteService = reporteService;
	}

	@GetMapping
	public ResponseEntity<?> ventasReporte(
		@RequestParam(name = "nombre_cliente", required = false) Optional<String> cliente,
		@RequestParam(name = "cliente", required = false) Optional<Integer> idCliente,
		@RequestParam(name = "anio", required = false) Optional<Integer> anio,
		@RequestParam(name = "mes", required = false) Optional<Integer> mes,
		@RequestParam(name = "dia", required = false) Optional<Integer> dia,
		@RequestParam(name = "pagado", required = false) Optional<Boolean> pagado,
		@RequestParam(name = "correo", required = false) String correo
	) {
		List<Venta> ventas = this.searchVentaService.searchVentas(
			idCliente.orElse(null),
			null,
			null,
			anio.orElse(null),
			mes.orElse(null),
			dia.orElse(null),
			pagado.orElse(null),
			null,
			null,
			null
		);
		if (ventas.isEmpty()) {
			return ResponseEntity.noContent().build();
		}
		String title = this.reporteService.makeReportTitle(
			cliente.orElse(null),
			anio.orElse(null),
			mes.orElse(null),
			dia.orElse(null),
			pagado.orElse(null)
		);
		this.reporteService.generateReporteFrom(ventas, title, correo);
		return new ResponseEntity<>(HttpStatus.CREATED);
	}
}
