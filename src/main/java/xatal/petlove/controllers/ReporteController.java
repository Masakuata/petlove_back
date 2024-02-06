package xatal.petlove.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import xatal.petlove.entities.Venta;
import xatal.petlove.reports.PDFVentaReports;
import xatal.petlove.services.ProductoService;
import xatal.petlove.services.SearchVentaService;
import xatal.petlove.util.Logger;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@CrossOrigin
@RestController
@RequestMapping("/reporte")
public class ReporteController {
	private final SearchVentaService searchVentaService;
	private final ProductoService productoService;

	public ReporteController(SearchVentaService searchVentaService, ProductoService productoService) {
		this.searchVentaService = searchVentaService;
		this.productoService = productoService;
	}

	@GetMapping
	public ResponseEntity<?> ventasReporte(
		@RequestParam(name = "id_cliente", required = false) Optional<Integer> idCliente,
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
			null
		);
		if (ventas.isEmpty()) {
			return ResponseEntity.noContent().build();
		}
		PDFVentaReports reports = new PDFVentaReports(this.productoService);
		try {
			reports.generateReportAndSend(ventas, correo);
		} catch (IOException e) {
			Logger.sendException(e);
			return ResponseEntity.internalServerError().build();
		}
		return new ResponseEntity<>(HttpStatus.CREATED);
	}
}
