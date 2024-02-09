package xatal.petlove.reports;

import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.antlr.v4.runtime.misc.Pair;
import org.springframework.stereotype.Component;
import xatal.petlove.entities.Cliente;
import xatal.petlove.entities.Producto;
import xatal.petlove.entities.Venta;
import xatal.petlove.mappers.ProductoMapper;
import xatal.petlove.mappers.VentaMapper;
import xatal.petlove.services.SearchProductoService;
import xatal.petlove.structures.Attachment;
import xatal.petlove.structures.MIMEType;
import xatal.petlove.util.Util;

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
	private static final String LOGO_PATH = "src/main/resources/pet-icon.png";
	private static final float TITLE_FONT_SIZE = 20F;
	private static final float DEFAULT_FONT_SIZE = 10F;

	private static final float LOGO_SQR_SIZE = 50F;
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

	public void generateReportAndSend(Venta venta) throws IOException {
		String email = venta.getCliente().getEmail();
		if (email == null || email.isEmpty()) {
			return;
		}
		String path = this.generateReportFrom(venta);
		if (path == null || path.isEmpty()) {
			return;
		}
		Cliente cliente = venta.getCliente();
		Path pathObj = Path.of(path);
		Attachment attachment = new Attachment(
			"venta.pdf",
			Files.readAllBytes(pathObj),
			MIMEType.APPLICATION_PDF
		);
		this.sendEmailWithAttachment(
			"Reporte de venta",
			"Adjunto a este correo se encuentra la venta recien realizada",
			cliente.getNombre(),
			cliente.getEmail(),
			attachment
		);
		Files.deleteIfExists(pathObj);
	}

	public String generateReportFrom(Venta venta) {
		if (venta == null) {
			return null;
		}
		String path;
		try {
			path = "file.pdf";
			PdfWriter writer = new PdfWriter(path);
			Document document = this.setupDocument(writer);
			document.add(this.getLogo());
			document.add(this.getAsTitle("PetLove"));
			document.add(this.getAsTitle("PRODUCTOS"));
			document.add(this.buildProductosTable(this.getVentaProductos(venta)));
			this.addTotal(venta).forEach(document::add);
			document.close();
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
		return path;
	}

	public String generateReportFrom(List<Venta> ventas, String title) {
		if (ventas.isEmpty()) {
			return null;
		}
		String path;
		try {
			path = "file.pdf";
			try (PdfWriter writer = new PdfWriter(path)) {
				Document document = this.setupDocument(writer);
				document.add(this.getLogo());
				document.add(this.getAsTitle("PetLove"));
				document.add(this.getAsTitle("Reporte de ventas\t" + title));
				document.add(this.getAsTitle("VENTAS"));
				document.add(this.buildVentasTable(ventas));
				document.add(this.getAsTitle("PRODUCTOS"));
				this.getProductosTables(ventas).forEach(block -> {
					document.add(block.a);
					document.add(block.b);
				});
				document.close();
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return path;
	}

	private Document setupDocument(PdfWriter writer) {
		PdfDocument pdf = new PdfDocument(writer);
		pdf.setDefaultPageSize(PageSize.LETTER);
		Document document = new Document(pdf);
		document.setFontSize(DEFAULT_FONT_SIZE);
		return document;
	}

	private Paragraph getAsTitle(String title) {
		return new Paragraph(title)
			.setFontSize(TITLE_FONT_SIZE);
	}

	private Image getLogo() throws MalformedURLException {
		Image image = new Image(ImageDataFactory.create(PDFVentaReports.LOGO_PATH));
		image.setHeight(LOGO_SQR_SIZE);
		image.setWidth(LOGO_SQR_SIZE);
		image.setFixedPosition(PageSize.LETTER.getRight() - LOGO_SQR_SIZE - 30F,
			PageSize.LETTER.getTop() - LOGO_SQR_SIZE - 40F);
		return image;
	}

	private Table buildVentasTable(List<Venta> ventas) {
		Table table = new Table(PDFVentaReports.VENTA_HEADERS.length);
		table.setWidth(UnitValue.createPercentValue(100));
		this.addHeaderCells(table, PDFVentaReports.VENTA_HEADERS);
		this.addVentaValues(ventas, table);
		return table;
	}

	private List<Pair<Paragraph, Table>> getProductosTables(List<Venta> ventas) {
		List<Pair<Paragraph, Table>> blocks = new LinkedList<>();

		ventas.forEach(venta -> {
			Table productosTable = this.buildProductosTable(this.getVentaProductos(venta));
			this.addTotal(venta).forEach(productosTable::addCell);
			Paragraph title =
				this.getAsTitle(venta.getCliente().getNombre() + ": " + Util.dateToString(venta.getFecha()));

			blocks.add(new Pair<>(title, productosTable));
		});
		return blocks;
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
			total.add(new Paragraph("$" + venta.getTotal()));

			Cell abonado = new Cell();
			abonado.add(new Paragraph("$" + venta.getAbonado()));

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
			precio.add(new Paragraph("$" + producto.getPrecio()));

			Cell cantidad = new Cell();
			cantidad.add(new Paragraph(String.valueOf(producto.getCantidad())));

			Cell subtotal = new Cell();
			subtotal.add(new Paragraph("$" + producto.getCantidad() * producto.getPrecio()));

			table.addCell(nombre);
			table.addCell(presentacion);
			table.addCell(mascota);
			table.addCell(precio);
			table.addCell(cantidad);
			table.addCell(subtotal);
		});
	}

	private List<Cell> addTotal(Venta venta) {
		Cell label = new Cell();
		Cell value = new Cell();
		label.add(new Paragraph("TOTAL VENTA"));
		value.add(new Paragraph(String.valueOf(venta.getTotal())));
		return List.of(label, value);
	}

	private List<Producto> getVentaProductos(Venta venta) {
		Map<Long, Producto> productosMap = ProductoMapper.mapIdProducto(
			this.searchProductoService.searchByIdsAndTipoCliente(
				this.ventaMapper.getIdProductos(venta),
				venta.getCliente().getTipoCliente()
			)
		);

		venta.getProductos().forEach(productoVenta -> {
			if (productosMap.containsKey(productoVenta.getId())) {
				productosMap.get(productoVenta.getId()).setCantidad(productoVenta.getCantidad());
			}
		});
		return productosMap.values().stream().toList();
	}
}
