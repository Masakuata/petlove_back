package xatal.petlove.reports;

import org.springframework.stereotype.Service;
import xatal.petlove.entities.Producto;
import xatal.petlove.entities.Venta;
import xatal.petlove.services.ProductoService;
import xatal.petlove.structures.Attachment;
import xatal.petlove.structures.MIMEType;
import xatal.petlove.util.Util;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class VentasReports extends XReport {
	private static final String VENTA_HEADER = "ID VENTA,CLIENTE,PAGADO,TOTAL,ABONADO,FECHA,FACTURADO\n";
	private static final String PRODUCTO_HEADER = "NOMBRE,PRESENTACION,TIPO MASCOTA,RAZA,PRECIO\n";
	private final ProductoService productoService;

	public VentasReports(ProductoService productoService) {
		this.productoService = productoService;
	}

	public void generateReportsFrom(List<Venta> ventas, String recipientName, String recipientEmail) {
		final List<Attachment> attachments = new LinkedList<>();
		attachments.add(new Attachment("ventas.csv", getVentasBytes(ventas), MIMEType.TEXT_CSV));
		attachments.addAll(this.getProductAttachments(ventas));

		this.sendEmailWithAttachment(
			"Reporte de Ventas",
			"Adjuntos se encuentran los reportes solicitados",
			recipientName,
			recipientEmail,
			attachments
		);
	}

	private List<Attachment> getProductAttachments(List<Venta> ventas) {
		return ventas
			.stream()
			.map(venta -> new Attachment(
				"productos.venta" + venta.getCliente().getNombre() + "." + Util.dateToString(venta.getFecha()) + ".csv",
				getProductosBytes(venta),
				MIMEType.TEXT_CSV
			))
			.collect(Collectors.toList());
	}

	private byte[] getVentasBytes(List<Venta> ventas) {
		String fileContent = ventas
			.stream()
			.map(this::formatVentaDetails)
			.collect(Collectors.joining());
		return (VentasReports.VENTA_HEADER + fileContent).getBytes();
	}

	private byte[] getProductosBytes(Venta venta) {
		String fileContent = this.getVentaProductos(venta)
			.stream()
			.map(this::formatProductoDetails)
			.collect(Collectors.joining());
		return (VentasReports.PRODUCTO_HEADER + fileContent).getBytes();
	}

	private List<Producto> getVentaProductos(Venta venta) {
		return venta.getProductos()
			.stream()
			.map(productoVenta ->
				this.productoService.getByIdAndTipoCliente(
					Math.toIntExact(productoVenta.getId()),
					venta.getCliente().getTipoCliente())
			)
			.filter(Optional::isPresent)
			.map(Optional::get)
			.collect(Collectors.toList());
	}

	private String formatProductoDetails(Producto producto) {
		return producto.getNombre() + ","
			+ producto.getPresentacion() + ","
			+ producto.getTipoMascota() + ","
			+ producto.getRaza() + ","
			+ producto.getPrecio() + "\n";
	}

	private String formatVentaDetails(Venta venta) {
		return venta.getId() + ","
			+ venta.getCliente().getNombre() + ","
			+ (venta.isPagado() ? "Si" : "NO") + ","
			+ venta.getTotal() + ","
			+ venta.getAbonado() + ","
			+ Util.dateToString(venta.getFecha()) + ","
			+ (venta.isFacturado() ? "SI" : "NO") + "\n";
	}
}
