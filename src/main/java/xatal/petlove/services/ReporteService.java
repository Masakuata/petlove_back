package xatal.petlove.services;

import org.springframework.stereotype.Service;
import xatal.petlove.entities.Venta;
import xatal.petlove.reports.PDFVentaReports;
import xatal.petlove.util.Logger;

import java.io.IOException;
import java.util.List;

@Service
public class ReporteService {
	private final PDFVentaReports pdfVentaReports;

	public ReporteService(ProductoService productoService) {
		this.pdfVentaReports = new PDFVentaReports(productoService);
	}

	public void generateReporteFrom(List<Venta> ventas, String reportTitle, String correo) {
		try {
			this.pdfVentaReports.generateReportAndSend(ventas, reportTitle, correo);
		} catch (IOException e) {
			Logger.sendException(e);
		}
	}

	public String makeReportTitle(
		String cliente,
		Integer anio,
		Integer mes,
		Integer dia,
		Boolean pagado
	) {
		StringBuilder title = new StringBuilder();
		if (cliente != null && !cliente.isEmpty()) {
			title.append(cliente).append("\t");
		}
		if (anio != null) {
			title.append(anio);
		}
		if (mes != null) {
			title.append("-").append(this.formatDateElement(mes));
		}
		if (dia != null) {
			title.append("-").append(this.formatDateElement(dia));
		}
		if (pagado != null) {
			if (pagado) {
				title.append(" : PAGADO");
			} else {
				title.append(" : NO PAGADO");
			}
		}
		return title.toString();
	}

	private String formatDateElement(Integer dateElement) {
		if (dateElement < 10) {
			return "0" + dateElement;
		}
		return dateElement.toString();
	}
}
