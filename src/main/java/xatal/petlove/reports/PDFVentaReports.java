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
import xatal.petlove.entities.Cliente;
import xatal.petlove.entities.Producto;
import xatal.petlove.entities.Venta;
import xatal.petlove.services.ProductoService;
import xatal.petlove.structures.Attachment;
import xatal.petlove.structures.MIMEType;
import xatal.petlove.util.Util;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static java.util.Arrays.stream;

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

	private final ProductoService productoService;

	public PDFVentaReports(ProductoService productoService) {
		this.productoService = productoService;
	}

	public void generateReportAndSend(List<Venta> ventas, String email) throws IOException {
		if (ventas.isEmpty()) {
			return;
		}
		if (email == null || email.isEmpty()) {
			return;
		}
		String path = this.generateReportFrom(ventas);
		if (path == null || path.isEmpty()) {
			return;
		}
		Path pathObj = Path.of(path);
		Attachment attachment = new Attachment(
			"venta.pdf",
			Files.readAllBytes(pathObj),
			MIMEType.APPLICATION_PDF
		);
		this.sendEmailWithAttachment(
			"Reporte de venta",
			"Adjunto a este correo se encuentra la venta recien realizada",
			null,
			email,
			attachment
		);
		Files.deleteIfExists(pathObj);
	}

	public boolean generateReportAndSend(Venta venta) throws IOException {
		String email = venta.getCliente().getEmail();
		if (email == null || email.isEmpty()) {
			return false;
		}
		String path = this.generateReportFrom(venta);
		if (path == null || path.isEmpty()) {
			return false;
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
		return true;
	}

	public String generateReportFrom(Venta venta) {
		if (venta == null) {
			return null;
		}
		String path;
		try {
			path = venta.getId().toString() + ".pdf";
			PdfWriter writer = new PdfWriter(path);
			Document document = this.setupDocument(writer);
			document.add(this.getLogo());
			document.add(this.getAsTitle("PetLove"));
			document.add(this.getAsTitle("PRODUCTOS"));
			document.add(this.buildProductosTable(this.getVentaProductos(venta)));
			document.add(this.addTotal(venta));
			document.close();
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
		return path;
	}

	public String generateReportFrom(List<Venta> ventas) {
		if (ventas.isEmpty()) {
			return null;
		}
		String path;
		try {
			path = ventas.get(0).getId().toString() + ".pdf";
			try (PdfWriter writer = new PdfWriter(path)) {
				Document document = this.setupDocument(writer);
				document.add(this.getLogo());
				document.add(this.getAsTitle("PetLove"));
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

		ventas.forEach(venta ->
			blocks.add(new Pair<>(
				this.getAsTitle(venta.getCliente().getNombre() + ": " + Util.dateToString(venta.getFecha())),
				this.buildProductosTable(this.getVentaProductos(venta))
			)));
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

	private Paragraph addTotal(Venta venta) {
		Paragraph total = new Paragraph();
		total.add("TOTAL VENTA:  " + venta.getTotal() + "\n");
		total.add("TOTAL PESO: " + this.productoService.getPesoVenta(venta));
		return total;
	}

	private List<Producto> getVentaProductos(Venta venta) {
		List<Producto> list = new ArrayList<>();
		venta.getProductos().forEach(productoVenta -> {
			Optional<Producto> byIdAndTipoCliente = this.productoService.searchByIdAndTipoCliente(
				productoVenta.getProducto(),
				venta.getCliente().getTipoCliente());
			if (byIdAndTipoCliente.isPresent()) {
				Producto producto = byIdAndTipoCliente.get();
				producto.setCantidad(productoVenta.getCantidad());
				list.add(producto);
			}
		});
		return list;
	}
}
