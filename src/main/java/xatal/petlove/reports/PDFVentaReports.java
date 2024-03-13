package xatal.petlove.reports;

import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.AreaBreak;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.layout.LayoutArea;
import com.itextpdf.layout.layout.LayoutContext;
import com.itextpdf.layout.layout.LayoutResult;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.renderer.IRenderer;
import org.springframework.stereotype.Component;
import xatal.petlove.entities.Producto;
import xatal.petlove.entities.Venta;
import xatal.petlove.mappers.ProductoMapper;
import xatal.petlove.mappers.VentaMapper;
import xatal.petlove.services.SearchProductoService;
import xatal.petlove.structures.Attachment;
import xatal.petlove.structures.MIMEType;
import xatal.petlove.util.Logger;
import xatal.petlove.util.Util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.stream;

@Component
public class PDFVentaReports extends XReport {
	private static final float SMALLER_FONT_SIZE = 8F;
	private static final String[] VENTA_HEADERS = {
		"ID VENTA", "CLIENTE", "PAGADO", "TOTAL", "ABONADO", "FECHA", "FACTURADO"
	};

	private static final String[] PRODUCTO_HEADERS = {
		"NOMBRE", "PRESENTACION", "TIPO MASCOTA", "PRECIO", "CANTIDAD", "SUBTOTAL"
	};

	private final SearchProductoService searchProductoService;
	private final VentaMapper ventaMapper;

	public PDFVentaReports(SearchProductoService searchProductoService, VentaMapper ventaMapper) {
		this.searchProductoService = searchProductoService;
		this.ventaMapper = ventaMapper;
	}

	public void generateReportAndSend(List<Venta> ventas, String title, String email) throws IOException {
		if (ventas.isEmpty()) {
			return;
		}
		if (email == null || email.isEmpty()) {
			return;
		}
		String path = this.generateReportFrom(ventas, title);
		if (path == null || path.isEmpty()) {
			return;
		}
		Path pathObj = Path.of(path);
		Attachment attachment = new Attachment(
			title + ".pdf",
			Files.readAllBytes(pathObj),
			MIMEType.APPLICATION_PDF
		);
		this.sendEmailWithAttachment(
			"Reporte: \t" + title,
			"",
			null,
			email,
			attachment
		);
		Files.deleteIfExists(pathObj);
	}

	public byte[] generateReport(Venta venta, String target) throws IOException {
		return this.generateReportAndSend(venta, target, false);
	}

	public byte[] generateReportAndSend(Venta venta, String target, boolean send) throws IOException {
		if (target == null || target.isEmpty()) {
			return null;
		}
		String path = this.createTicket(venta);
		if (path == null || path.isEmpty()) {
			return null;
		}
		Path pathObj = Path.of(path);
		byte[] pdfBytes = Files.readAllBytes(pathObj);
		if (send) {
			Attachment attachment = new Attachment(
				"venta.pdf",
				pdfBytes,
				MIMEType.APPLICATION_PDF
			);
			this.sendEmailWithAttachment(
				"Reporte de venta",
				"Adjunto a este correo se encuentra la venta recien realizada",
				"",
				target,
				attachment
			);
		}
		Files.deleteIfExists(pathObj);
		return pdfBytes;
	}

	public String createTicket(Venta venta) {
		if (venta == null) {
			return null;
		}
		String path = "file.pdf";
		Document finalDocument;
		try (Document document = PDFDocument.setupNewTicket(new PdfWriter(path))) {
			Table main = new Table(2);
			main.setFontSize(PDFDocument.TITLE_FONT_SIZE);
			main.setWidth(UnitValue.createPercentValue(100F));
			main.addCell(
				PDFDocument.getNoBorderCell("FOMENTO ECONOMICO HEMBRA MACHO", 1, 2)
					.setTextAlignment(TextAlignment.CENTER));
			main.addCell(PDFDocument.getNoBorderCell("FEH211116NK4"));
			main.addCell(PDFDocument.getNoBorderCell(Util.dateToString(venta.getFecha()))
				.setTextAlignment(TextAlignment.RIGHT));
			main.addCell(PDFDocument.getNoBorderCell(venta.getCliente().getNombre(), 1, 2));
			venta.getCliente().getDireccionById(venta.getDireccion()).ifPresent(direccion ->
				main.addCell(PDFDocument.getNoBorderCell(direccion.getDireccion(), 1, 2)));
			document.add(main);

			Table productTable = this.ticketProductos(this.getVentaProductos(venta));
			this.addTotal(venta, productTable.getNumberOfColumns()).forEach(productTable::addCell);
			document.add(productTable);

			float elementsHeight = this.getTablesHeight(List.of(main, productTable), document.getRenderer());
			finalDocument = PDFDocument.setupNewTicket(new PdfWriter(path), elementsHeight);
			finalDocument.setMargins(0, 0, 0, 0);
			finalDocument.add(main);
			finalDocument.add(productTable);
		} catch (IOException ex) {
			Logger.sendException(ex);
			return null;
		}
		finalDocument.flush();
		finalDocument.close();
		return path;
	}

	public String generateReportFrom(List<Venta> ventas, String title) {
		if (ventas.isEmpty()) {
			return null;
		}
		String path = "file.pdf";
		try (Document document = PDFDocument.setupNewDocument(new PdfWriter(path))) {
			document.add(PDFDocument.getLogo());
			document.add(PDFDocument.getAsTitle("PetLove"));
			document.add(PDFDocument.getAsTitle("Reporte de ventas\t" + title));
			document.add(PDFDocument.getAsTitle("VENTAS"));
			document.add(this.buildVentasTable(ventas));
			document.add(this.totalVentasResume(ventas));
			document.add(new AreaBreak());
			document.add(PDFDocument.getAsTitle("PRODUCTOS"));
			this.getProductosTables(ventas).forEach(document::add);
		} catch (FileNotFoundException e) {
			Logger.sendException(e);
			return null;
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}

		return path;
	}

	private Table buildVentasTable(List<Venta> ventas) {
		Table table = new Table(PDFVentaReports.VENTA_HEADERS.length);
		table.setWidth(UnitValue.createPercentValue(100));
		this.addHeaderCells(table, PDFVentaReports.VENTA_HEADERS);
		this.addVentaValues(ventas, table);
		return table;
	}

	private List<Table> getProductosTables(List<Venta> ventas) {
		List<Table> tables = new LinkedList<>();

		ventas.forEach(venta -> {
			Table titleTable = new Table(2).setWidth(UnitValue.createPercentValue(100F));
			titleTable.addCell(PDFDocument.getNoBorderCell(venta.getCliente().getNombre())
				.setTextAlignment(TextAlignment.LEFT).setFontSize(PDFDocument.TITLE_FONT_SIZE));
			titleTable.addCell(PDFDocument.getBottomBorderCell(Util.dateToString(venta.getFecha()))
				.setTextAlignment(TextAlignment.RIGHT).setFontSize(PDFDocument.TITLE_FONT_SIZE)
				.setWidth(UnitValue.createPercentValue(20F)));
			titleTable.setKeepWithNext(true);

			Table productosTable = this.buildProductosTable(this.getVentaProductos(venta));
			productosTable.setKeepTogether(true);

			this.addTotal(venta, productosTable.getNumberOfColumns()).forEach(productosTable::addCell);
			Paragraph title = PDFDocument.emptyNewLine().add(
				PDFDocument.getAsTitle(venta.getCliente().getNombre() + ": " + Util.dateToString(venta.getFecha())));
			title.setKeepWithNext(true);

			tables.add(titleTable);
			tables.add(productosTable);
		});
		return tables;
	}

	private Table ticketProductos(List<Producto> productos) {
		Table table = new Table(3);
		table.setWidth(UnitValue.createPercentValue(100F));
		table.setFontSize(PDFDocument.DEFAULT_FONT_SIZE);
		table.setTextAlignment(TextAlignment.CENTER);
		productos.forEach(producto -> {
			Cell nombre = PDFDocument.getBottomBorderCell(
					"[" + producto.getCantidad() + "] " + producto.getNombre() + "\n" + producto.getPresentacion())
				.setTextAlignment(TextAlignment.LEFT);
			Cell precio = PDFDocument.getBottomBorderCell(Util.formatMoney(producto.getPrecio()))
				.setTextAlignment(TextAlignment.RIGHT);
			Cell subtotal = PDFDocument.getBottomBorderCell(Util.formatMoney(producto.getCantidad() * producto.getPrecio()))
				.setTextAlignment(TextAlignment.RIGHT);
			table.addCell(nombre);
			table.addCell(precio);
			table.addCell(subtotal);
		});
		return table;
	}

	private Table buildProductosTable(List<Producto> productos) {
		Table table = new Table(PDFVentaReports.PRODUCTO_HEADERS.length);
		table.setWidth(UnitValue.createPercentValue(100F));
		this.addHeaderCells(table, PDFVentaReports.PRODUCTO_HEADERS);
		this.addProductosValues(productos, table);
		return table;
	}

	private void addHeaderCells(Table table, String[] headers) {
		stream(headers).forEach(ventaHeader -> {
			Cell cell = new Cell();
			Paragraph text = new Paragraph(ventaHeader);
			text.setTextAlignment(TextAlignment.CENTER);
			text.setFontSize(SMALLER_FONT_SIZE);
			text.setBold();
			cell.add(text);
			table.addHeaderCell(cell);
		});
	}

	private void addVentaValues(List<Venta> ventas, Table table) {
		ventas.forEach(venta -> {
			Cell ventaId = new Cell();
			ventaId.add(new Paragraph(String.valueOf(venta.getId())));

			Cell cliente = new Cell();
			cliente.add(new Paragraph(venta.getCliente().getNombre()));

			Cell pagado = new Cell();
			pagado.add(new Paragraph(venta.isPagado() ? "SI" : "NO"));

			Cell total = new Cell();
			total.add(new Paragraph(Util.formatMoney(venta.getTotal()))).setTextAlignment(TextAlignment.RIGHT);

			Cell abonado = new Cell();
			abonado.add(new Paragraph(Util.formatMoney(venta.getAbonado())).setTextAlignment(TextAlignment.RIGHT));

			Cell fecha = new Cell();
			fecha.add(new Paragraph(Util.dateToString(venta.getFecha())));

			Cell facturado = new Cell();
			facturado.add(new Paragraph(venta.isFacturado() ? "SI" : "NO"));

			table.addCell(ventaId);
			table.addCell(cliente);
			table.addCell(pagado);
			table.addCell(total);
			table.addCell(abonado);
			table.addCell(fecha);
			table.addCell(facturado);
		});
	}

	private void addProductosValues(List<Producto> productos, Table table) {
		productos.forEach(producto -> {
			Cell nombre = new Cell();
			nombre.add(new Paragraph(producto.getNombre()));

			Cell presentacion = new Cell();
			presentacion.add(new Paragraph(producto.getPresentacion()));

			Cell mascota = new Cell();
			mascota.add(new Paragraph(producto.getTipoMascota()));

			Cell precio = new Cell();
			precio.add(new Paragraph(Util.formatMoney(producto.getPrecio())).setTextAlignment(TextAlignment.RIGHT));

			Cell cantidad = new Cell();
			cantidad.add(new Paragraph(String.valueOf(producto.getCantidad())));

			Cell subtotal = new Cell();
			subtotal.add(new Paragraph(Util.formatMoney(producto.getCantidad() * producto.getPrecio())).setTextAlignment(TextAlignment.RIGHT));

			table.addCell(nombre);
			table.addCell(presentacion);
			table.addCell(mascota);
			table.addCell(precio);
			table.addCell(cantidad);
			table.addCell(subtotal);
		});
	}

	private List<Cell> addTotal(Venta venta, int cellsWidth) {
		Cell total = new Cell(0, cellsWidth - 1).setBorder(Border.NO_BORDER).setBorderTop(new SolidBorder(1F));
		Cell totalValue = PDFDocument.getTopBorderCell(Util.formatMoney(venta.getTotal()))
			.setTextAlignment(TextAlignment.RIGHT);
		Cell abonado = new Cell(0, cellsWidth - 1).setBorder(Border.NO_BORDER);
		Cell abonadoValue = PDFDocument.getNoBorderCell(Util.formatMoney(venta.getAbonado())).setTextAlignment(TextAlignment.RIGHT);
		Cell restante = new Cell(0, cellsWidth - 1).setBorder(Border.NO_BORDER).setBorderTop(new SolidBorder(1F));
		Cell restanteValue = PDFDocument.getTopBorderCell(Util.formatMoney(venta.getTotal() - venta.getAbonado()))
			.setTextAlignment(TextAlignment.RIGHT);
		abonado.add(new Paragraph("ABONADO"));
		total.add(new Paragraph("TOTAL VENTA"));
		restante.add(new Paragraph("RESTANTE"));

		return List.of(total, totalValue, abonado, abonadoValue, restante, restanteValue);
	}

	private Paragraph totalVentasResume(List<Venta> ventas) {
		float total = ventas
			.stream()
			.map(Venta::getTotal)
			.reduce(0F, Float::sum);
		float abonado = ventas
			.stream()
			.map(Venta::getAbonado)
			.reduce(0F, Float::sum);
		return PDFDocument.getAsTitle("TOTAL VENTAS: " + Util.formatMoney(total) + "\n")
			.add(PDFDocument.getAsTitle("TOTAL ABONADO: " + Util.formatMoney(abonado) + "\n"))
			.add(PDFDocument.getAsTitle("TOTAL POR LIQUIDAR: " + Util.formatMoney(total - abonado)))
			.setTextAlignment(TextAlignment.RIGHT);
	}

	private float getTablesHeight(List<Table> tables, IRenderer parent) {
		return tables
			.stream()
			.map(table -> {
				LayoutResult result = table.createRendererSubTree().setParent(parent).layout(
					new LayoutContext(new LayoutArea(1, new Rectangle(0, 0, 400, 1e4f))));
				return result.getOccupiedArea().getBBox().getHeight();
			})
			.reduce(0F, Float::sum);
	}

	private List<Producto> getVentaProductos(Venta venta) {
		Map<Long, Producto> productosMap = ProductoMapper.mapIdProducto(
			this.searchProductoService.searchByIdsAndTipoCliente(
				this.ventaMapper.getIdProductos(venta),
				venta.getCliente().getTipoCliente()
			)
		);

		venta.getProductos().forEach(productoVenta -> {
			if (productosMap.containsKey(productoVenta.getProducto())) {
				productosMap.get(productoVenta.getProducto()).setCantidad(productoVenta.getCantidad());
			}
		});
		return productosMap.values().stream().toList();
	}
}
