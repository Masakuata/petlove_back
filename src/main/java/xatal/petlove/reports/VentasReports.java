package xatal.petlove.reports;

import org.springframework.stereotype.Service;
import xatal.petlove.entities.Producto;
import xatal.petlove.entities.Venta;
import xatal.petlove.services.ProductoService;
import xatal.petlove.services.SearchProductoService;
import xatal.petlove.structures.Attachment;
import xatal.petlove.structures.MIMEType;
import xatal.petlove.util.Util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class VentasReports extends XReport {
	private static final String VENTA_HEADER = "ID VENTA,CLIENTE,PAGADO,TOTAL,ABONADO,FECHA,FACTURADO\n";
	private static final String PRODUCTO_HEADER = "NOMBRE,PRESENTACION,TIPO MASCOTA,RAZA,PRECIO\n";
	private final ProductoService productoService;
	private final SearchProductoService searchProductoService;

	public VentasReports(ProductoService productoService, SearchProductoService searchProductoService) {
		this.productoService = productoService;
		this.searchProductoService = searchProductoService;
	}

	public void generateReportsFrom(Object reportable) {
		List<Field> reportableFields = Arrays.stream(reportable.getClass().getDeclaredFields())
			.filter(field -> field.isAnnotationPresent(ReportableField.class))
			.toList();
		String header = this.getReportableHeader(reportableFields);
		String values;
		Optional<String> optionalValues = this.getFieldValues(reportable, reportableFields);
		if (optionalValues.isPresent()) {
			values = optionalValues.get();
		}

	}

	public String getReportableHeader(List<Field> reportableFields) {
		StringBuilder header = new StringBuilder();
		reportableFields.forEach(field -> header.append(field.getAnnotation(ReportableField.class).headerName()));
		header.append("\n");
		return header.toString();
	}

	public Optional<String> getFieldValues(Object reportable, List<Field> reportableFields) {
		StringBuilder values = new StringBuilder();
		for (Field field : reportableFields) {
			field.setAccessible(true);
			try {
				if (!field.getAnnotation(ReportableField.class).getValueFrom().isEmpty()) {
					String method = field.getAnnotation(ReportableField.class).getValueFrom();
					values.append((String) reportable.getClass().getMethod(method).invoke(reportable));
				} else {
					if (field.getAnnotation(ReportableField.class).isDate()) {
						values.append(Util.dateToString((Date) field.get(reportable)));
					} else {
						values.append(field.get(reportable)).append(",");
					}
				}
			} catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
				xatal.petlove.util.Logger.sendException(e);
				return Optional.empty();
			}
		}
		values.append("\n");
		return values.toString().describeConstable();
	}

//	public Optional<String> getListValues(Object reportable) {
//
//	}

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
				this.getProductosBytes(venta),
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
				this.searchProductoService.searchByIdAndTipoCliente(
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
